package groove.verify;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.Random;

/**
 * @author Mohammad Javad Mehrabi
 */

public abstract class RLAgent {
    private int stateSize;
    private int actionSize;
    protected int batchSize;
    private int hiddenLayerCount;
    protected float gamma;
    private float epsilon; //exploration rate
    private float epsilonMin;
    private float epsilonDecay;
    private float learningRate;
    protected boolean prioritizedExperienceReplayMemory;
    private int[] hiddenLayersNeuron;
    protected IExperienceReplayMemory memory;
    protected MultiLayerNetwork model;
    protected MultiLayerNetwork targetModel;
    public RLAgent(int stateSize, int actionSize, int ReplayMemorySize, float discountFactor, float epsilonMin, float epsilonDecay, float learningRate, int hiddenLayerCount, int[] hiddenLayersNeuron, boolean prioritizedExperienceReplayMemory, int batchSize) {
        this.stateSize = stateSize;
        this.actionSize = actionSize;
        this.gamma = discountFactor;
        this.epsilon = 1f;
        this.epsilonMin = epsilonMin;
        this.epsilonDecay = epsilonDecay;
        this.learningRate = learningRate;
        this.batchSize = batchSize;
        this.hiddenLayerCount = hiddenLayerCount;
        this.hiddenLayersNeuron = hiddenLayersNeuron;
        this.prioritizedExperienceReplayMemory = prioritizedExperienceReplayMemory;
        if (prioritizedExperienceReplayMemory)
            this.memory = new PrioritizedExperienceReplayMemory(ReplayMemorySize);
        else
            this.memory = new SimpleExperienceReplayMemory(ReplayMemorySize);
        this.model = createModel();
        this.targetModel = model.clone();
    }
    private MultiLayerNetwork createModel() {
        int layerCount = 0;
        if (this.hiddenLayersNeuron.length < this.hiddenLayerCount) {
            for (int i = hiddenLayerCount - hiddenLayersNeuron.length - 1; i < 2; i++) {
                hiddenLayersNeuron[i] = this.stateSize * 2/3;
            }
        }
        NeuralNetConfiguration.ListBuilder conf = new NeuralNetConfiguration.Builder()
                .updater(new RmsProp(this.learningRate))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(this.stateSize)
                        .nOut(hiddenLayersNeuron[0])
                        .weightInit(WeightInit.ZERO)
                        .build());
        for (layerCount = 2; layerCount<=this.hiddenLayerCount; layerCount++) {
            conf.layer(layerCount - 1, new DenseLayer.Builder()
                    .nIn(hiddenLayersNeuron[layerCount - 2])
                    .nOut(hiddenLayersNeuron[layerCount - 1])
                    .activation(Activation.RELU)
                    .weightInit(WeightInit.ZERO)
                    .build());
        }
        conf.layer(hiddenLayerCount, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .activation(Activation.IDENTITY)
                .nIn(hiddenLayersNeuron[hiddenLayersNeuron.length - 1])
                .nOut(actionSize)
                .weightInit(WeightInit.ZERO)
                .build());
        conf.build();
        MultiLayerConfiguration configuration = conf.build();
        MultiLayerNetwork model = new MultiLayerNetwork(configuration);
        model.init();
        return model;
    }
    public void remember(INDArray curState, int action, float reward, INDArray nextState, boolean done, int actionSize) {}
    public void replay() {
        if (this.epsilon > this.epsilonMin)
            this.epsilon *= this.epsilonDecay;
    }
    public int actEGreedy(INDArray state, int actionCount) {
        int action;
        Random random = new Random();
//        if (limited != 0)
//            action = random.nextInt(limited);
        if (random.nextFloat() <= this.epsilon)
            action = random.nextInt(actionCount);
        else {
            action = argMax(model.output(state), actionCount);
        }
        return action;
    }
    protected int argMax(INDArray values, int actionSize) {
        int max = 0;
        for (int i = 1; i < actionSize; i++) {
            if (values.getFloat(0, i) > values.getFloat(0, max))
                max = i;
        }
        return max;
    }
    protected int argMax(INDArray values) {
        int max = 0;
        for (int i = 1; i < actionSize; i++) {
            if (values.getFloat(0, i) > values.getFloat(0, max))
                max = i;
        }
        return max;
    }
    protected int argMin(INDArray values, int actionSize) {
        int min = 0;
        for (int i = 1; i < actionSize; i++) {
            if (values.getFloat(0, i) < values.getFloat(0, min))
                min = i;
        }
        return min;
    }
    protected int argMin(INDArray values) {
        int min = 0;
        for (int i = 1; i < actionSize; i++) {
            if (values.getFloat(0, i) < values.getFloat(0, min))
                min = i;
        }
        return min;
    }
    public void update_targetModel() {
        this.targetModel = model.clone();
    }
    public int getReplayMemorySize() {
        return this.memory.getSize();
    }
    public void loadWeights(String filename) {
        try {
            this.model = ModelSerializer.restoreMultiLayerNetwork(filename, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveWeights(String filename) {
        try {
            ModelSerializer.writeModel(this.model, filename,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
