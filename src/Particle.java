import java.security.SecureRandom;
import java.util.List;

/**
 * Created by FuBaR on 12/12/2016.
 */
public class Particle extends Chromosome {
    public float[] velocities;
    public float[] personalBest;

    public Particle(List<Item> allItems, SecureRandom random) {
        super(allItems, random);
        initializeVelocities(random);
    }

    public Particle(Particle particle) {
        super(particle.amounts, particle.items);
        //this.velocities = particle.velocities;
        //this.personalBest = particle.personalBest;
        this.fitness = particle.fitness;
        this.cost = particle.cost;
        this.requirementsError = particle.requirementsError;
        this.satisfied = particle.satisfied;
    }

    private void initializeVelocities(SecureRandom random) {
        float min = -1.0f, max = 1.0f;
        velocities = new float[amounts.length];
        personalBest = amounts;
        for (int i = 0; i < velocities.length; i++) {
            velocities[i] = Math.round((min + random.nextFloat() * (max - min)) * 100.0f) / 100.0f;
        }
    }

    @Override
    public void calculateFitness() {
        float oldFitness = fitness;
        super.calculateFitness();
        if (fitness <= oldFitness)
            personalBest = amounts;
    }
}
