package groove.verify;

/**
 * @author Mohammad Javad Mehrabi
 */
// SumTree = A Tree That The Value Of Each Parent = Sum Of It`s Children
public class SumTree {
    private int maxSize;
    private double[] tree;
    private Memory[] memories;
    private int index;
    private int size;

    public SumTree(int capacity) {
        this.index = 0;
        this.maxSize = capacity;
        this.tree = new double[2 * capacity - 1]; // Because We Add On Leafs = 2 * n - 1
        this.memories = new Memory[capacity];
    }

    // Update To The Root Node
    //            0
    //        1       2
    // Parent =  (n - 1) / 2
    // Children =  (n * 2) + 1
    private void propagate(int childIndex, double change) {
        int parent = (childIndex - 1) / 2;
        tree[parent] += change;
        if (parent != 0) {
            propagate(parent, change);
        }
    }

    public int getSize() {
        return this.size;
    }
    //Find Sample On Leaf Node
    private int retrieve(int parentIndex, double s) {
        int left = parentIndex * 2 + 1;
        int right = left + 1;

        if (left >= this.tree.length) // If It Was Leaf Itself And Doesn't Have Any Child
            return parentIndex;

        if (s <= this.tree[left]) // Are Left Leafs Are Smaller Than This
            return this.retrieve(left, s);
        else
            return this.retrieve(right, s - this.tree[left]);
    }

    public double getTotal() {
        return this.tree[0];
    }

    //Store Priority And Sample
    public void add(double priority, Memory sample) {
        // Add On Leaf => Leaf Index = n + MaxSize - 1 - First Add On First Leaf To The End
        int sIndex = this.index + this.maxSize - 1;

        this.memories[this.index] = sample;
        this.update(sIndex, priority);

        this.size = ((this.size + 1 < maxSize) ? size + 1 : maxSize);
        this.index = (this.index + 1) % maxSize;
    }

    //Update Priority
    public void update(int index, double priority) {
        double change = priority - this.tree[index];
        this.tree[index] = priority;
        this.propagate(index, change);
    }

    //Get Priority And Sample
    public PERMemoryModel get(double s) {
        int index = this.retrieve(0, s);
        int dataIndex = index - this.maxSize + 1;
        return new PERMemoryModel(index, this.tree[index], this.memories[dataIndex]);
    }
}
