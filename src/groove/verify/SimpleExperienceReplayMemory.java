package groove.verify;

import java.util.Random;

/**
 * @author Mohammad Javad Mehrabi
 */

public class SimpleExperienceReplayMemory implements IExperienceReplayMemory {
    private Memory[] buffer;
    private int maxSize;
    private int index;
    private int size;
    public SimpleExperienceReplayMemory(int maxSize) {
        this.buffer = new Memory[maxSize];
        this.maxSize = maxSize;
        this.index = 0;
        this.size = 0;
    }
    public void append(Memory sample, double error) {
        this.buffer[this.index] = sample;
        this.size = ((this.size + 1 < maxSize) ? size + 1 : maxSize);
        this.index = (this.index + 1) % maxSize;
    }

    public int getSize() {
        return this.size;
    }
    public Memory[] getRandomSample(int batchSize) {
        Random random = new Random();
        Memory[] outMemory = new Memory[batchSize];
        for (int i =0; i < batchSize; i++) {
            outMemory[i] = (buffer[random.nextInt(size)]);
        }
        return outMemory;
    }
}
