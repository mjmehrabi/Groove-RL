package groove.verify;

import java.util.Random;

/**
 * @author Mohammad Javad Mehrabi
 */

public class PrioritizedExperienceReplayMemory implements IExperienceReplayMemory {
    private double epsilon;
    private double alpha;
    private double beta;
    private double beta_increment_per_sampling;
    private SumTree sumTree;
    public PrioritizedExperienceReplayMemory(int capacity) {
        this.epsilon = 0.01;
        this.alpha = 0.6;
        this.beta = 0.4;
        this.beta_increment_per_sampling = 0.001f;
        this.sumTree = new SumTree(capacity);
    }
    private double getPriority(double error) {
        return Math.pow(Math.abs(error) + this.epsilon, this.alpha);
    }
    public void append(Memory sample, double error) {
        double priority = this.getPriority(error);
        this.sumTree.add(priority, sample);
    }
    public PERMemoryModel[] getSample(int batchSize) {
        double segment = this.sumTree.getTotal() / batchSize;
        this.beta = Math.min(1, this.beta + this.beta_increment_per_sampling);
        PERMemoryModel[] PERMemoryModel = new PERMemoryModel[batchSize];
        for (int i=0; i < batchSize; i++) {
            Random random = new Random();
            double from = segment * i;
            double to = segment * (i+1);
            double s = random.nextDouble() * (from - to) + from;
            PERMemoryModel[i] = this.sumTree.get(s);
        }
        return PERMemoryModel;
    }
    public void update(int index, double error) {
        double priority = this.getPriority(error);
        this.sumTree.update(index, priority);
    }
    public int getSize() {
        return this.sumTree.getSize();
    }
}
