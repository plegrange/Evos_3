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

/**
 * Created by FuBaR on 12/11/2016.
 */
public class DifferentialEvolution {
    private List<Chromosome> chromosomes, nextGen;
    private int P = 100;
    private float scalingFactor = 1.0f, crossoverRate = 0.2f;
    float[] minimumRequirements;
    List<Item> items;
    private int iterations;
    private String fitnessFile = "fitnessesDE.xls", costsFile = "costsDE.xls";
    private float[] genFitnesses, genCosts;

    public DifferentialEvolution() {
        chromosomes = new ArrayList<>();
    }

    public void run(List<Item> items, int iterations) {
        genFitnesses = new float[iterations];
        genCosts = new float[iterations];
        initializePopulation(items);
        this.iterations = iterations;
        for (int i = 1; i < iterations; i++) {
            nextGen = new ArrayList<>();
            chromosomes = sortList(chromosomes);
            testPopulation(i);
            crossoverPopulation();
        }
    }

    private void crossoverPopulation() {
        for (int i = 0; i < P; i++) {
            Chromosome individual = chromosomes.remove(i);
            //testIndividual(individual);
            createTrialVector(individual.amounts, individual.fitness);
            chromosomes.add(i, individual);
        }
        chromosomes = nextGen;
    }

    private void createTrialVector(float[] vector, float fitness) {
        SecureRandom random = new SecureRandom();
        Chromosome randomOne = chromosomes.get(random.nextInt(chromosomes.size()));
        Chromosome randomTwo = chromosomes.get(random.nextInt(chromosomes.size()));
        float[] trialVec = new float[vector.length];
        for (int i = 0; i < trialVec.length; i++) {
            trialVec[i] = Math.max(vector[i] + scalingFactor * (randomTwo.amounts[i] - randomOne.amounts[i]), 0.0f);
        }
        Chromosome newIndividual = new Chromosome(binomialCrossover(vector, trialVec), items);
        testIndividual(newIndividual);
        if (newIndividual.fitness < fitness)
            nextGen.add(newIndividual);
        else
            nextGen.add(new Chromosome(vector, items));
    }

    private float[] binomialCrossover(float[] individual, float[] trialVec) {
        float[] newVec = new float[trialVec.length];
        for (int i = 0; i < individual.length; i++) {
            if (Math.random() <= (double) crossoverRate)
                newVec[i] = trialVec[i];
            else
                newVec[i] = individual[i];

        }
        return newVec;
    }

    private void initializePopulation(List<Item> items) {
        this.items = items;
        Item temp = items.get(0);
        SecureRandom random = new SecureRandom();
        minimumRequirements = new float[temp.components.size()];
        for (int i = 0; i < temp.components.size(); i++) {
            minimumRequirements[i] = temp.components.get(i).recommendedAnnually;
        }
        for (int i = 0; i < P; i++) {
            chromosomes.add(new Chromosome(items, random));
        }
    }

    private void testPopulation(int gen) {
        System.out.print("Generation : " + gen);
        float bestFitness = 99999.0f;
        Chromosome best = chromosomes.get(0);
        for (Chromosome individual : chromosomes) {
            testIndividual(individual);
            if (individual.fitness < bestFitness) {
                bestFitness = individual.fitness;
                best = individual;
            }
        }
        genFitnesses[gen] = bestFitness;
        genCosts[gen] = best.cost;
        System.out.print(" | Best Fitness : " + bestFitness);
        System.out.print(" | Best ReqError : " + best.requirementsError);
        System.out.print(" | Cost = $" + best.cost);
        System.out.print(" | Gain/Cost : " + best.gain / best.cost);
        System.out.println(" | Satisfied = " + best.satisfied);
        if (gen == iterations - 1) {
            System.out.println(" -> Best Fitness : " + bestFitness + " Cost : " + best.cost + "\n" + best.getItemAmountDisplayString()
                    + "\n" + best.getComponentAmountDisplayString());
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

    private void testIndividual(Chromosome individual) {
        // float a = 10.0f, b = 100.0f;
        float[] totalComponentAmounts = individual.calculateComponents();
        float cost = individual.calculateCost();
        float requirementsError = getRequirementsError(totalComponentAmounts, individual);
        individual.cost = cost;
        individual.requirementsError = requirementsError;
        individual.calculateFitness();
    }

    private float getRequirementsError(float[] totals, Chromosome individual) {
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

    private List<Chromosome> sortList(List<Chromosome> list) {
        List<Chromosome> sortedList = new ArrayList<>();
        while (list.size() > 0) {
            Chromosome selected = list.remove(0);
            sortedList = insertSorted(sortedList, selected);
        }
        return sortedList;
    }

    private List<Chromosome> insertSorted(List<Chromosome> list, Chromosome item) {
        if (list.size() == 0) {
            list.add(item);
            return list;
        } else if (list.size() == 1) {
            Chromosome chromosome = list.get(0);
            if (chromosome.fitness > item.fitness) {
                list.add(0, item);
            } else list.add(1, item);
            return list;
        } else {
            Chromosome left, right;
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
