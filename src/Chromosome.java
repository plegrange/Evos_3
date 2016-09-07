import java.util.Random;

/**
 * Created by s213391244 on 2016/08/31.
 */
public class Chromosome {
    double[] weights;
    double[] powers;
    public double fitness;

    public Chromosome(int size) {
        initializeWeights(size);
    }

    public Chromosome(double[] weights, double[] powers) {
        this.weights = weights;
        this.powers = powers;
    }

    private void initializeWeights(int size) {
        weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = getRandomWeight();
        }
        powers = new double[size];
        for (int i = 0; i < size; i++) {
            powers[i] = getRandomPower();
        }
    }

    public double getValue(double[] attributes) {
        double sum = 0;
        int i;
        for (i = 0; i < attributes.length - 1; i++) {
            sum += Math.pow(attributes[i], powers[i]) * weights[i];
        }
        return sum + weights[7];
    }

    public void test(double[] attributes, double[] min, double[] max) {
        double[] pattern = new double[attributes.length - 1];
        for (int i = 0; i < attributes.length - 1; i++) {
            pattern[i] = attributes[i + 1];
        }
        double value = getValue(pattern);
        System.out.println(value + " -> " + attributes[0]);
    }

    private double denormalize(double value, double min, double max) {
        return min + (max - min) * value;
    }

    public double getError(double[] attributes) {
        double[] pattern = new double[attributes.length - 1];
        for (int i = 0; i < attributes.length - 1; i++) {
            pattern[i] = attributes[i + 1];
        }
        return Math.pow(getValue(pattern) - attributes[0], 2);
    }

    private double getRandomWeight() {
        Random random = new Random();
        double min = -1000, max = 1000;
        return min + (max - min) * random.nextDouble();
    }

    private double getRandomPower() {
        Random random = new Random();
        double min = 0, max = 3;
        return min + (max - min) * random.nextDouble();
    }

    double mutationRate = 0.1;

    public void mutate() {
        Random random = new Random();
        for (int index = 0; index < weights.length; index++) {
            if (random.nextDouble() <= mutationRate) {
                weights[index] = mutateValue(weights[index]);
            }
            if (random.nextDouble() <= mutationRate) {
                powers[index] = mutatePowerValue(powers[index]);
            }
        }
    }

    private double mutateValue(double value) {
        double mutationConstant = 200;
        double min = value - mutationConstant, max = value + mutationConstant;
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }

    private double mutatePowerValue(double value) {
        double mutationConstant = 1;
        double min = value - mutationConstant, max = value + mutationConstant;
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }

    public void displayFitness(int size) {
        System.out.println(Math.sqrt(fitness) / size);
    }

    public double getFitness(int size) {
        return Math.sqrt(fitness) / size;
    }

    public void displayWeights() {
        for (int i = 0; i < 8; i++) {
            System.out.print(weights[i] + " ");
        }
        System.out.println();
        for (int i = 0; i < 8; i++) {
            System.out.print(powers[i] + " ");
        }
        System.out.println();
    }

}
