package groove.verify;

/**
 * @author Mohammad Javad Mehrabi
 */

public class PERMemoryModel {
    private int id;
    private double priority;
    private Memory data;
    public PERMemoryModel(int id, double priority, Memory data) {
        this.id = id;
        this.priority = priority;
        this.data = data;
    }
    public int getId() {
        return id;
    }
    public Memory getData() {
        return data;
    }
    public double getPriority() {
        return priority;
    }
}
