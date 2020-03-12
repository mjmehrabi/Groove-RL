package groove.verify;

/**
 * @author Mohammad Javad Mehrabi
 */

public interface IExperienceReplayMemory {
    public int getSize();
    public void append(Memory sample, double error);
}
