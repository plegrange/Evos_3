import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by s213391244 on 2016/08/31.
 */
public class GeneticAlgorithm {
    int P = 100;
    int alpha = P / 5;
    List<double[]> training, testing;
    List<Chromosome> chromosomes;

    public GeneticAlgorithm(List<double[]> training, List<double[]> testing, double[] min, double[] max) {
        this.training = training;
        this.testing = testing;
        //normalizeValues(min, max);
    }


    private void normalizeValues(double[] min, double[] max) {
        for (double[] pattern : training) {
            for (int i = 0; i < 8; i++) {
                pattern[i] = normalizeValue(min[i], max[i], pattern[i]);
            }
        }
        for (double[] pattern : testing) {
            for (int i = 0; i < 8; i++) {
                pattern[i] = normalizeValue(min[i], max[i], pattern[i]);
            }
        }
    }

    private double normalizeValue(double min, double max, double value) {
        return (value - min) / (max - min);
    }

    public void run(double[] min, double[] max) {
        initializePopulation(min, max);
        displayFitness(chromosomes, training.size());
        for (int i = 0; i < 5000; i++) {
            List<Chromosome> crossoverList = crossoverSelection(chromosomes);
            crossoverList = testList(crossoverList, min, max);
            //chromosomes = merge(chromosomes, crossoverList);
            List<Chromosome> mutationList = mutationSelection(crossoverList);
            mutationList = testList(mutationList, min, max);
            chromosomes = merge(chromosomes, mutationList);
            chromosomes = selectNewPopulation(chromosomes);
            System.out.println(i);
            System.out.println("Average: " + getAverageFitness() + "  |  Best: " + chromosomes.get(0).fitness);
        }
        chromosomes = sortList(chromosomes);
        displayFitness(chromosomes, training.size());
        test(min, max);
    }

    private double getAverageFitness() {
        double sum = 0;
        for (Chromosome chromosome : chromosomes) {
            sum += chromosome.fitness;
        }
        return sum / chromosomes.size();
    }

    private void test(double[] min, double[] max) {
        Chromosome best = chromosomes.get(0);
        for (double[] pattern : testing) {
            best.test(pattern, min, max);
        }
    }

    private void initializePopulation(double[] min, double[] max) {
        chromosomes = new ArrayList<>();
        for (int i = 0; i < P; i++) {
            chromosomes.add(new Chromosome(8));
        }
        chromosomes = testList(chromosomes, min, max);
    }

    double crossoverRate = 1;

    private List<Chromosome> crossoverSelection(List<Chromosome> list) {
        List<Chromosome> newList = new ArrayList<>();
        Random random = new Random();
        while (newList.size() < P) {
            Chromosome A = selectRandom(list);
            Chromosome B = selectRandom(list);
            //if (random.nextDouble() > crossoverRate)
            Chromosome child = crossover(A, B);
            testFitness(training, child);
            if (child.fitness > A.fitness || child.fitness > B.fitness) {
                newList.add(child);
            }
        }
        return newList;
    }

    private Chromosome crossover(Chromosome A, Chromosome B) {
        double[] newWeights = new double[8];
        double[] newPowers = new double[8];
        double a = A.fitness / (A.fitness + B.fitness);
        double b = B.fitness / (A.fitness + B.fitness);
        for (int i = 0; i < newWeights.length; i++) {
            newWeights[i] = a * A.weights[i] + b * B.weights[i];
            newPowers[i] = a * A.powers[i] + b * B.powers[i];
        }
        return new Chromosome(newWeights, newPowers);
    }

    private List<Chromosome> mutationSelection(List<Chromosome> list) {
        List<Chromosome> mutationList = new ArrayList<>();
        while (mutationList.size() < P) {
            Chromosome selected = list.remove(0);
            selected.mutate();
            mutationList.add(selected);
        }
        return mutationList;
    }

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

    private List<Chromosome> selectNewPopulation(List<Chromosome> chromosomes) {
        chromosomes = sortList(chromosomes);
        List<Chromosome> newPopulation = new ArrayList<>();
        for (int i = 0; i < alpha; i++) {
            newPopulation.add(chromosomes.remove(0));
        }
        for (int i = 0; i < P - alpha; i++) {
            Chromosome selected = selectRandom(chromosomes);
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
}
