package groove.verify;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * @author Mohammad Javad Mehrabi
 */

public class DDQNAgent extends RLAgent {

    public DDQNAgent(int stateSize, int actionSize, int ReplayMemorySize, float discountFactor, float epsilonMin, float epsilonDecay, float learningRate, int hiddenLayerCount, int[] hiddenLayersNeuron, boolean prioritizedExperienceReplayMemory, int batchSize) {
        super(stateSize, actionSize, ReplayMemorySize, discountFactor, epsilonMin, epsilonDecay, learningRate, hiddenLayerCount, hiddenLayersNeuron, prioritizedExperienceReplayMemory, batchSize);
    }

    @Override
    public void replay() {
        super.replay();
        if (this.prioritizedExperienceReplayMemory) {
            prioritizedReplay();
        } else {
            simpleReplay();
        }
    }
    private void simpleReplay() {
        for (Memory tempMemory:((SimpleExperienceReplayMemory)memory).getRandomSample(this.batchSize)) {
            float target = 0;
            if (!tempMemory.isTerminal()) {
                try {
                    int nextBestAction= argMax(this.model.output(tempMemory.getNextState()));
                    INDArray target_f2 = this.targetModel.output(tempMemory.getNextState());
                    target = (tempMemory.getReward() + this.gamma * target_f2.getFloat(0, nextBestAction));
                } catch (Exception e) {
                    e.printStackTrace();
                    target = tempMemory.getReward();
                }
            } else {
                target = tempMemory.getReward();
            }
            try {
                INDArray target_f = this.model.output(tempMemory.getCurState());
                target_f.putScalar(0, tempMemory.getAction(), target);
                this.model.fit(tempMemory.getCurState(), target_f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prioritizedReplay() {
        for (PERMemoryModel tempMemory:((PrioritizedExperienceReplayMemory)memory).getSample(batchSize)) {
            float target = 0;
            Memory data = tempMemory.getData();
            if (!data.isTerminal()) {
                try {
                    int nextBestAction= argMax(this.model.output(data.getNextState()));
                    INDArray target_f2 = this.targetModel.output(data.getNextState());
                    target = (data.getReward() + this.gamma * target_f2.getFloat(0, nextBestAction));
                } catch (Exception e) {
                    e.printStackTrace();
                    target = data.getReward();
                }
            } else {
                target = data.getReward();
            }
            double error = Math.abs(this.model.output(data.getCurState()).getDouble(0, data.getAction()) - target);
            ((PrioritizedExperienceReplayMemory)memory).update(tempMemory.getId(), error);
            try {
                INDArray target_f = this.model.output(data.getCurState());
                target_f.putScalar(0, data.getAction(), target);
                this.model.fit(data.getCurState(), target_f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void remember(INDArray curState, int action, float reward, INDArray nextState, boolean done, int actionSize) {
        super.remember(curState, action, reward, nextState, done, actionSize);
        memory.append(new Memory(curState, action, reward, nextState, done, actionSize), 0);
    }
}
