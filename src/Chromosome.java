import java.util.Random;

/**
 * Created by s213391244 on 2016/08/31.
 */
public class Chromosome {
    double[] attributes;
    double[] weights;

    public Chromosome(double[] attributes) {
        this.attributes = attributes;
        initializeWeights(attributes.length);
    }

    private void initializeWeights(int size) {
        weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = getRandomWeight();
        }
    }

    public double getValue() {
        double sum = 0;
        for (int i = 1; i < attributes.length; i++) {
            sum += attributes[i] * weights[i];
        }
        return sum;
    }

    private double getRandomWeight() {
        Random random = new Random();
        double min = -1, max = 1;
        return min + (max - min) * random.nextDouble();
    }

    public void mutate() {
        Random random = new Random();
        int index = random.nextInt(attributes.length);
        attributes[index] = mutateValue(attributes[index]);
    }

    private double mutateValue(double value) {
        double mutationConstant = 1 / Math.sqrt(value);
        double min = value - mutationConstant, max = value + mutationConstant;
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }
}
