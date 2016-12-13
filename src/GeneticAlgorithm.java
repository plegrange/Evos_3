import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by s213391244 on 2016/08/31.
 */
public class GeneticAlgorithm {
    int P = 100;
    int alpha = 20;
    List<Item> items;
    List<Chromosome> chromosomes, children;
    float[] minimumRequirements;
    float mutationRate = 0.125f, mutationDistance;
    private int iterations;
    private String fitnessFile = "fitnesses.xls", costsFile = "costs.xls";
    private float[] genFitnesses, genCosts;

    public GeneticAlgorithm() {
        chromosomes = new ArrayList<>();
    }

    public void run(List<Item> items, int iterations) {
        mutationDistance = Math.round(1.8f * 100.0f) / 100.0f;

        genFitnesses = new float[iterations];
        genCosts = new float[iterations];
        this.iterations = iterations;
        initializePopulation(items);
        for (int i = 1; i < iterations; i++) {
            testPopulation(i);
            createChildPopulation();
            mutateChildPopulation(i);
            testChildren();
            selectNewPopulation();
            if (i % 1000 == 0 && mutationDistance > 0.015f)
                mutationDistance -= Math.round(0.01f * 100.0f) / 100.0f;
            mutationDistance = Math.round(mutationDistance * 100.0f) / 100.0f;
        }
    }

    private void initializePopulation(List<Item> items) {
        this.items = items;
        Item temp = items.get(0);
        minimumRequirements = new float[temp.components.size()];
        for (int i = 0; i < temp.components.size(); i++) {
            minimumRequirements[i] = temp.components.get(i).recommendedAnnually;
        }
        for (int i = 0; i < P; i++) {
            chromosomes.add(new Chromosome(items));
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
        System.out.print(" | Mutation Distance : $" + mutationDistance);
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

    private void testChildren() {
        for (Chromosome individual : children) {
            testIndividual(individual);
            //System.out.println("Fitness : " + individual.fitness);
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

    private void createChildPopulation() {
        children = new ArrayList<>();
        Random random = new Random();
        chromosomes = sortList(chromosomes);
        while (children.size() < P) {
            Chromosome A = chromosomes.get(random.nextInt(P));
            Chromosome B = chromosomes.get(random.nextInt(P));
            //if (random.nextDouble() > crossoverRate)
            Chromosome child = crossover(A, B);
            //testFitness(training, child);
            //if (child.fitness > A.fitness || child.fitness > B.fitness) {
            children.add(child);
            //}
        }
    }

    private Chromosome crossover(Chromosome A, Chromosome B) {
        float[] newAmounts = new float[A.amounts.length];
        float a = A.fitness / (A.fitness + B.fitness);
        float b = B.fitness / (A.fitness + B.fitness);
        for (int i = 0; i < newAmounts.length; i++) {
            newAmounts[i] = Math.round((b * A.amounts[i] + a * B.amounts[i]) * 100.0f) / 100.0f;
        }
        return new Chromosome(newAmounts, items);
    }

    private void mutateChildPopulation(int gen) {
        Random random = new Random();
        for (Chromosome individual : children) {
            int i = random.nextInt(individual.amounts.length);
            testIndividual(individual);
            individual.amounts[i] = Math.round(mutateValue(individual.amounts[i], gen, individual) * 100.0f) / 100.0f;
        }
    }

    private float mutateValue(float value, int gen, Chromosome individual) {
        Random random = new Random();
        testIndividual(individual);
        float newValue;
        if (random.nextBoolean())
            newValue = value + mutationDistance;
        else
            newValue = value - mutationDistance;
        if (newValue < 0.0f)
            newValue = 0.0f;
        return newValue;
    }

    private void selectNewPopulation() {
        Random random = new Random();
        List<Chromosome> parents;
        parents = merge(chromosomes, children);
        parents = sortList(parents);
        chromosomes = new ArrayList<>();
        for (int i = 0; i < alpha; i++) {
            chromosomes.add(parents.get(i).cloneChromosome());
        }
        for (int i = alpha; i < P; i++) {
            chromosomes.add(parents.get(alpha + random.nextInt(2 * P - alpha)).cloneChromosome());
        }
        children = new ArrayList<>();
    }

    /*
        private List<Chromosome> mutationSelection(List<Chromosome> list) {
            List<Chromosome> mutationList = new ArrayList<>();
            while (mutationList.size() < P) {
                Chromosome selected = list.remove(0);
                selected.mutate();
                mutationList.add(selected);
            }
            return mutationList;
        }
    */
    private List<Chromosome> merge(List<Chromosome> A, List<Chromosome> B) {
        while (B.size() > 0) {
            A.add(B.remove(0));
        }
        return sortList(A);
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

    /*
        private List<Chromosome> selectNewPopulation(List<Chromosome> chromosomes) {
            chromosomes = sortList(chromosomes);
            List<Chromosome> newPopulation = new ArrayList<>();
            for (int i = 0; i < alpha; i++) {
                newPopulation.add(chromosomes.remove(0));
            }
            Random random = new Random();
            for (int i = 0; i < P - alpha; i++) {
                Chromosome selected;
                selected = chromosomes.get(random.nextInt(chromosomes.size() - 10));
                chromosomes.remove(selected);
                newPopulation.add(selected);
            }
            return sortList(newPopulation);
        }

        private Chromosome selectBest(List<Chromosome> list) {
            return list.get(0);
        }

        private Chromosome selectRandom(List<Chromosome> list) {
            Random random = new Random();
            int index = random.nextInt(list.size());
            return list.get(index);
        }

        private List<Chromosome> testList(List<Chromosome> list, double[] min, double[] max) {
            for (Chromosome chromosome : list) {
                testFitness(training, chromosome);
            }
            return list;
        }

        public void evaluate(List<double[]> patterns) {
            System.out.println("Evaluation: ");
            Chromosome best = chromosomes.get(0);
            for (double[] pattern : patterns) {
                System.out.println(best.getValue(pattern));
            }
        }

        private void testFitness(List<double[]> patterns, Chromosome chromosome) {
            double fitness = 0;
            for (double[] pattern : patterns) {
                fitness += chromosome.getError(pattern);
            }
            chromosome.fitness = fitness;
            //return fitness;
        }

        private void displayFitness(List<Chromosome> list, int size) {
            for (Chromosome chromosome : list) {
                chromosome.displayFitness(size);
            }
        }

        private String outputFile = "model1.xls";
    */
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
}
