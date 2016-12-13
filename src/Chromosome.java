import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by s213391244 on 2016/08/31.
 */
public class Chromosome {
    public float[] amounts;
    public float fitness, cost, requirementsError, gain;
    public List<Item> items;
    // public boolean[] satisfied;
    public boolean satisfied;

    public Chromosome(List<Item> allItems) {
        initializeAmounts(allItems.size());
        items = new ArrayList<>();
        for (Item item : allItems) {
            items.add(item.cloneItem());
        }
        satisfied = false;//new boolean[amounts.length];
    }
    public Chromosome(List<Item> allItems, SecureRandom random) {
        initializeAmounts(allItems.size(), random);
        items = new ArrayList<>();
        for (Item item : allItems) {
            items.add(item.cloneItem());
        }
        satisfied = false;//new boolean[amounts.length];
    }

    public Chromosome(float[] newAmounts, List<Item> items) {
        amounts = newAmounts;
        this.items = new ArrayList<>();
        for (Item item : items) {
            this.items.add(item.cloneItem());
        }
        satisfied = false;//new boolean[amounts.length];
    }

    public Chromosome cloneChromosome() {
        return new Chromosome(this.amounts, this.items);
    }

    public void calculateFitness() {
        float a = 10000.0f;
        float b = 1.0f;
        float c = 0.0f;
        countGain();
        if (requirementsError <= 0.0f) {

            a = 0.01f;
            //c = 0.1f;
        }
        fitness = a * requirementsError + b * (cost + 10.0f);
        if (satisfied)
            fitness = b * cost;
    }

    private void countGain() {
        gain = 0.0f;
        int counter = 0;
        for (int i = 0; i < items.size(); i++) {
            gain += items.get(i).gainPerDollar() * amounts[i];
            if (amounts[i] > 0.0f)
                counter++;
        }
        //gain /= counter;
    }

    private void initializeAmounts(int size) {
        amounts = new float[size];
        Random random = new Random();
        float zeroRate = random.nextFloat() * 0.2f, max = 10.0f, min = 0.0f;
        for (int i = 0; i < size; i++) {
            amounts[i] = min + (max - min) * random.nextFloat();
        }
    }

    private void initializeAmounts(int size, SecureRandom random) {
        amounts = new float[size];
        //Random random = new Random();
        float zeroRate = random.nextFloat() * 0.2f, max = 10.0f, min = 0.0f;
        for (int i = 0; i < size; i++) {
            amounts[i] = min + (max - min) * random.nextFloat();
        }
    }

    public float[] calculateComponents() {
        float[] totalComponentAmounts = new float[items.get(0).components.size()];
        float[] componentAmounts;
        for (int j = 0; j < items.size(); j++) {
            Item item = items.get(j);
            componentAmounts = item.getComponentAmounts();
            for (int i = 0; i < componentAmounts.length; i++) {
                totalComponentAmounts[i] += componentAmounts[i] * amounts[j];
            }
        }
        return totalComponentAmounts;
    }

    public float calculateCost() {
        cost = 0.0f;
        for (int i = 0; i < items.size(); i++) {
            cost += amounts[i];
        }
        return Math.round(cost * 100.0f) / 100.0f;
    }

    private float getRandomAmount() {
        Random random = new Random();
        float min = 0.0f, max = 10000.0f;
        return min + (max - min) * random.nextFloat();
    }

    public String getItemAmountDisplayString() {
        String amountDisplayString = "Item Amounts: \n";
        for (int i = 0; i < amounts.length; i++) {
            amountDisplayString = amountDisplayString + "\n" + items.get(i).itemName + " " + (Math.round(amounts[i] * 100.0f) / 100.0f);
        }
        return amountDisplayString;
    }

    public String getComponentAmountDisplayString() {
        float[] componentAmounts = new float[items.get(0).components.size()];
        for (int j = 0; j < amounts.length; j++) {
            Item item = items.get(j);
            float[] temp = item.getComponentAmounts();
            for (int i = 0; i < temp.length; i++) {
                componentAmounts[i] += temp[i] * amounts[j];
            }
        }
        String amountDisplayString = "Component Amounts: \n";
        for (int i = 0; i < componentAmounts.length; i++) {
            amountDisplayString = amountDisplayString + "\n" + componentAmounts[i];
        }
        return amountDisplayString;
    }
}
