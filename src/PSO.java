import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by FuBaR on 12/12/2016.
 */
public class PSO {
    private List<Particle> swarm;
    private List<Item> items;
    private int numberOfParicles = 100;
    private int iterations;
    private String fitnessFile = "fitnessesPSO.xls", costsFile = "costsPSO.xls";
    private float[] genFitnesses, genCosts;
    float[] minimumRequirements;
    private Particle globalBest;

    public PSO() {
        swarm = new ArrayList<>();
    }

    public void run(List<Item> items, int iterations) {
        this.iterations = iterations;
        this.items = items;
        genFitnesses = new float[iterations];
        genCosts = new float[iterations];
        initializeSwarm();
        for (int i = 1; i < iterations; i++) {
            swarm = sortList(swarm);
            testPopulation(i);
            updateSwarm();
        }
    }

    private float c1 = 0.1f, c2 = 0.1f;

    private void updateSwarm() {
        Random random = new Random();
        for (Particle particle : swarm) {
            for (int i = 0; i < particle.velocities.length; i++) {
                particle.velocities[i] = (float) (particle.velocities[i] +
                        (float) c1 * random.nextFloat() * (particle.personalBest[i] - particle.amounts[i]) +
                        (float) c2 * random.nextFloat() * (globalBestAmounts[i] - particle.amounts[i]));
                particle.amounts[i] = particle.amounts[i] + particle.velocities[i];
                if (particle.amounts[i] < 0.0f)
                    particle.amounts[i] = 0.0f;
            }
            testIndividual(particle);
        }
    }

    private void initializeSwarm() {
        Item temp = items.get(0);
        minimumRequirements = new float[temp.components.size()];
        for (int i = 0; i < temp.components.size(); i++) {
            minimumRequirements[i] = temp.components.get(i).recommendedAnnually;
        }
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < numberOfParicles; i++) {
            swarm.add(new Particle(items, random));
        }
        globalBestAmounts = swarm.get(0).amounts;
    }

    float globalBestFitness = 9999999;
    float[] globalBestAmounts;
    Particle globalBestParticle;

    private void testPopulation(int gen) {
        System.out.print("Generation : " + gen);
        float bestFitness = 99999.0f;
        //Particle best = swarm.get(0);
        for (Particle individual : swarm) {
            testIndividual(individual);
            if (individual.fitness < globalBestFitness) {
                globalBestParticle = new Particle(individual);
                //globalBestParticle.calculateFitness();
                globalBestFitness = globalBestParticle.fitness;
                globalBestAmounts = globalBestParticle.amounts;
            }
        }

        genFitnesses[gen] = globalBestFitness;
        genCosts[gen] = globalBestParticle.cost;
        System.out.print(" | Best Fitness : " + globalBestFitness);
        System.out.print(" | Best ReqError : " + globalBestParticle.requirementsError);
        System.out.print(" | Cost = $" + globalBestParticle.cost);
        System.out.print(" | Gain/Cost : " + globalBestParticle.gain / globalBestParticle.cost);
        System.out.println(" | Satisfied = " + globalBestParticle.satisfied);
        if (gen == iterations - 1) {
            System.out.println(" -> Best Fitness : " + globalBestFitness + " Cost : " + globalBestParticle.cost + "\n" + globalBestParticle.getItemAmountDisplayString()
                    + "\n" + globalBestParticle.getComponentAmountDisplayString());
            try {
                write(genFitnesses, fitnessFile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
            try {
                write(genCosts, costsFile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }
    }

    private void testIndividual(Particle individual) {
        // float a = 10.0f, b = 100.0f;
        float[] totalComponentAmounts = individual.calculateComponents();
        float cost = individual.calculateCost();
        float requirementsError = getRequirementsError(totalComponentAmounts, individual);
        individual.cost = cost;
        individual.requirementsError = requirementsError;
        individual.calculateFitness();
    }

    private float getRequirementsError(float[] totals, Particle individual) {
        float reqError = 0.0f * totals.length;
        int numberSatisfied = 0;
        boolean moveUp;
        individual.satisfied = true;
        float a = 1.0f, b = 1.0f, c = 0.01f;
        for (int i = 1; i < totals.length; i++) {
            if (totals[i] >= minimumRequirements[i] * 1.0f)
                reqError += a * ((totals[i] - minimumRequirements[i] * 1.0f) / (minimumRequirements[i] * 1.0f));
            else {
                reqError += b * ((1.0f * minimumRequirements[i] - totals[i]) / totals[i]);
                individual.satisfied = false;
            }
        }
        return (reqError);
    }

    private void write(float[] fitnesses, String output) throws IOException, WriteException {
        File file = new File(output);
        WorkbookSettings wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("en", "EN"));

        WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
        workbook.createSheet("Report", 0);
        WritableSheet excelSheet = workbook.getSheet(0);
        int col = 0;
        for (int i = 0; i < fitnesses.length; i++) {
            if (i % 60000 == 0) {
                col++;
            }
            Number newCell = new Number(col, i % 60000, fitnesses[i]);
            excelSheet.addCell(newCell);
        }

        workbook.write();
        workbook.close();
    }

    private List<Particle> sortList(List<Particle> list) {
        List<Particle> sortedList = new ArrayList<>();
        while (list.size() > 0) {
            Particle selected = list.remove(0);
            sortedList = insertSorted(sortedList, selected);
        }
        return sortedList;
    }

    private List<Particle> insertSorted(List<Particle> list, Particle item) {
        if (list.size() == 0) {
            list.add(item);
            return list;
        } else if (list.size() == 1) {
            Particle particle = list.get(0);
            if (particle.fitness > item.fitness) {
                list.add(0, item);
            } else list.add(1, item);
            return list;
        } else {
            Particle left, right;
            for (int i = 0; i < list.size() - 1; i++) {
                left = list.get(i);
                right = list.get(i + 1);
                if (item.fitness < left.fitness) {
                    list.add(i, item);
                    return list;
                } else if (item.fitness < right.fitness) {
                    list.add(i + 1, item);
                    return list;
                } else if (i + 1 == list.size() - 1) {
                    list.add(i + 2, item);
                    return list;
                }
            }
            return list;
        }
    }
}
