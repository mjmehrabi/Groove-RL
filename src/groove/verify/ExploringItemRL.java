package groove.verify;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import groove.explore.ExploreType;
import groove.grammar.Grammar;
import groove.grammar.QualName;
import  groove.grammar.Rule;
import groove.grammar.model.GrammarModel;
import groove.grammar.rule.RuleEdge;
import groove.grammar.rule.RuleNode;
import groove.gui.Simulator;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.lts.MatchResult;
import groove.verify.Exploringinfo;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *  @author Mohammad Javad Mehrabi
 *
 */

public class ExploringItemRL {

    public ExploringItemRL(){
        RulesName=new ArrayList<QualName>();
        allNACs=new ArrayList<NAC>();
        Alltype=new ArrayList<QualName>();
        Number_Explored_States=0;
        allActionsUntilNow=new ArrayList<Integer>();
        allinfo=new ArrayList<Exploringinfo>();
        tempStates=new ArrayList<ExploringItemRL.TempState>();
        goalStatesInfo=new ArrayList<GoalState>();

        redBlocks = new ArrayList<>();
        greenBlocks = new ArrayList<>();
        blueBlocks = new ArrayList<>();
        blockRelationship = new ArrayList<>();
        allBlocks = new ArrayList<>();
        blocks = new HashMap<>();
        pacmanTargetPosition = new HashMap<>();
        pacmanElement = new HashMap<>();
    }

    // for i:  allprevStates[i]--allRules[i]-->allcurStates[i];

    //Config
    public int RulesCount;
    public int episodes;
    public float rewards;
    public int toMaxStep;
    public int maxStepIncrement;
    public int batchSize;
    public int fromMaxStep;
    public int maxStateSize;
    public int maxActionOutput;
    public int experienceReplayMemorySize;
    public int targetModelUpdateStep;
    public int hiddenLayerCount;
    public int dqnAgentType;
    public int rewardType;
    public boolean memoryType;
    public int[] hiddenLayersNeuronSize;
    public float discountFactor;
    public float epsilonMin;
    public float epsilonDecay;
    public float learningRate;
    public int timeLimit;
    public long lastTime;
    public RLAgent dqnAgent;
    //------
    //Heuristics
    public String[] positions;
    public ArrayList<String> redBlocks;
    public ArrayList<String> greenBlocks;
    public ArrayList<String> blueBlocks;
    public ArrayList<String> blockRelationship;
    public ArrayList<String> allBlocks;
    public Map<String, String> blocks;
    public Map<String, String> pacmanTargetPosition;
    public Map<String, String> pacmanElement;
    public String table;
    public int ispoint;
    //---------


    public ArrayList<QualName> RulesName;


    public String targetRule="";

    public ArrayList<RuleEdge> targetGraph_edgeList=new ArrayList<RuleEdge>();
    public ArrayList<RuleNode> targetGraph_nodeList=new ArrayList<RuleNode>();


    public Simulator simulator;

    public GTS gts=null;

    public boolean callFromHeuGenerator=false;
    public boolean init=false;

    public GraphState initialState=null;
    public Grammar grammer=null;
    public GrammarModel grammermodel=null;
    public ExploreType exploreType=null;

    public String heuristicResult;

    public GTS gtsLearning;

    ///Is the progress of exploring is visible
    public boolean isProgressVisible;
    public boolean findTheBestGoal = false;
    public String CTLproperty="reachability";

    public GraphState lastStateInReachability;
    public long Number_Explored_States;
    public long First_Found_Reach_depth;
    public ArrayList<Integer> allActionsUntilNow;
    public ArrayList<QualName> Alltype;
    public ArrayList<Exploringinfo> allinfo;

    public class TempState{
        public TempState(){
            allNextStates=new  ArrayList<GraphState>();
            allRuleNames=new  ArrayList<String>();
            allActionsUntilNow = new ArrayList<>();
        }
        public GraphState curstate;
        public int depth;
        public List<MatchResult> matches;
        public ArrayList<GraphState> allNextStates;
        public ArrayList<String> allRuleNames;
        public INDArray ReshapedCurrentState;
        public ArrayList<Integer> allActionsUntilNow;
    }

    public ExploringItemRL.TempState getNewTempState(){
        return new TempState();
    }

    public ArrayList<ExploringItemRL.TempState> tempStates;

    public ArrayList<ExploringItemRL.GoalState> goalStatesInfo;
    public ExploringItemRL.GoalState getNewGoalState(){
        return new ExploringItemRL.GoalState();
    }
    public class GoalState{
        public long foundTime=0;
        public long witnessLength=0;
        public long exploredstate=0;
    }

    public NAC getNewNAC(){
        return new NAC();
    }


    public ArrayList<NAC> allNACs;


    public class NAC{
        public NAC(){
            ruleedgeList=new ArrayList<RuleEdge>();
            rulenodeList=new ArrayList<RuleNode>();
            ANacEqualNodes=new ArrayList<NacEqualNode>();
        }
        public ArrayList<RuleEdge> ruleedgeList;
        public ArrayList<RuleNode> rulenodeList;
        public ArrayList<NacEqualNode>  ANacEqualNodes;
    }
    public NacEqualNode getNewNacEqualNode(){
        return new NacEqualNode();
    }
    public class NacEqualNode{
        public NacEqualNode(){
            HEList=new ArrayList<String>();
        }
        public RuleNode tNode;
        public ArrayList<String> HEList;
    }

}