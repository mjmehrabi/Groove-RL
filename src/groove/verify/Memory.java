package groove.verify;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * @author Mohammad Javad Mehrabi
 */

public class Memory {
    private INDArray curState;
    private int action;
    private float reward;
    private INDArray nextState;
    private int actionSize;
    private boolean terminal;

    public INDArray getCurState() {
        return curState;
    }

    public void setCurState(INDArray curState) {
        this.curState = curState;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public float getReward() {
        return reward;
    }

    public void setReward(float reward) {
        this.reward = reward;
    }

    public INDArray getNextState() {
        return nextState;
    }

    public void setNextState(INDArray nextState) {
        this.nextState = nextState;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public int getActionSize() {
        return actionSize;
    }

    public void setActionSize(int actionSize) {
        this.actionSize = actionSize;
    }

    public Memory(INDArray curState, int action, float reward, INDArray nextState, boolean terminal, int actionSize) {
        this.curState = curState;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
        this.terminal = terminal;
        this.actionSize = actionSize;
    }
}
