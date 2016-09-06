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
        return sum + weights[i];
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
        double min = -100, max = 100;
        return min + (max - min) * random.nextDouble();
    }

    private double getRandomPower() {
        Random random = new Random();
        double min = 0, max = 1;
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
        double mutationConstant = 0.01;
        double min = value - mutationConstant, max = value + mutationConstant;
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }

    private double mutatePowerValue(double value) {
        double mutationConstant = 0.001;
        double min = value, max = value + mutationConstant;
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }

    public void displayFitness(int size) {
        System.out.println(fitness / size);
    }

}
