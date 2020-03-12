package groove.verify;

import groove.explore.ExploreType;
import groove.grammar.Condition;
import groove.grammar.Grammar;
import groove.grammar.QualName;

import java.io.IOException;
import java.util.*;


import groove.grammar.aspect.AspectEdge;
import groove.grammar.host.HostEdge;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.model.RuleModel;
import groove.grammar.rule.RuleEdge;
import groove.grammar.rule.RuleNode;
import groove.grammar.type.TypeLabel;
import groove.gui.Simulator;
import groove.gui.SimulatorModel;
import groove.gui.action.HeuLearnFromBFSExploreAction;
import groove.gui.action.RLExploreAction;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.util.parse.FormatException;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;

/**
 *  @author Mohammad Javad Mehrabi
 *
 */



public class RL{
    public RL(){
        exploringItems=new ExploringItemRL();
    }

    public void EnableSelectedHostGraph(){
        try {
            simulator.getModel().doEnableUniquely(ResourceKind.HOST, QualName.name(HostGraphName));
        } catch (IOException exc) {
            System.err.println("Error during %s enabling" +ResourceKind.HOST.getDescription());
        }

    }

    public String Explore(String targetRule,int RulesCount,ArrayList<QualName> RulesName,Grammar grammer,GrammarModel grammermodel){


        if(!callFromHeuGenerator){
            grammermodel=simulator.getModel().getGrammar();

        }

        //////////////////////////////////////////////////////
        if(callFromHeuGenerator){



            RulesName=new ArrayList<QualName>();

            Set<QualName> sname=grammermodel.getNames(ResourceKind.RULE);


            Iterator<QualName> it=sname.iterator();
            while(it.hasNext())
            {
                QualName ts=it.next();
                RuleModel rulemodel=grammermodel.getRuleModel(ts);
                if(rulemodel.isEnabled()){
                    Set<? extends AspectEdge> edgeSet=rulemodel.getSource().edgeSet();

                    boolean flag=false;
                    for(AspectEdge ae:edgeSet ){
                        //	        			if(ae.toString().contains("new:") ||ae.toString().contains("del:") || ae.toString().contains("not:") ){
                        if(ae.toString().contains("new:") ||ae.toString().contains("del:")  ){
                            flag=true;
                            break;
                        }
                    }
                    if(!flag){
                        try{
                            if(rulemodel.toResource().getAnchor().size()>0)
                                flag=true;
                        }
                        catch (FormatException e) {
                            // do nothing
                            e.printStackTrace();
                        }
                    }

                    if(flag){
                        RulesCount++;
                        RulesName.add(ts);
                    }

                }
            }

        }

        /////////////////////////////


        exploringItems=new ExploringItemRL();
        exploringItems.callFromHeuGenerator=callFromHeuGenerator;
        exploringItems.grammer=grammer;
        exploringItems.grammermodel=grammermodel;
        ExploreType exploreType=null;
        if(!callFromHeuGenerator){
            simulator.getModel().resetGTS();  //Creates a fresh GTS and fires an update event.
            GTS gts = simulator.getModel().getGTS();
            exploringItems.initialState=gts.startState();
            final SimulatorModel simulatorModel = simulator.getModel();
            exploreType=simulatorModel.getExploreType();
        }else{
            GTS gts=null;
            try {
                gts = new GTS(grammer);
            } catch (FormatException e) {
                // do nothing
                e.printStackTrace();
            }
            exploringItems.gts=gts;
            exploringItems.initialState=gts.startState();
            exploringItems.exploreType=exploreType;
        }

        exploringItems.Number_Explored_States=0;
        exploringItems.CTLproperty=CTLproperty;   //deadlock || reachability ||safetyByReach || liveByCycle ||liveByDeadlock
        exploringItems.targetRule=targetRule;  //modelcheckingType
        exploringItems.Alltype=Alltype;

        exploringItems.episodes = this.episodes;
        exploringItems.fromMaxStep = this.fromMaxStep;
        exploringItems.toMaxStep = this.toMaxStep;
        exploringItems.maxStepIncrement = this.maxStepIncrement;
        exploringItems.batchSize = this.batchSize;
        exploringItems.maxStateSize = this.maxStateSize;
        exploringItems.maxActionOutput = this.maxActionOutput;
        exploringItems.experienceReplayMemorySize = this.experienceReplayMemorySize;
        exploringItems.discountFactor = this.discountFactor;
        exploringItems.epsilonMin = this.epsilonMin;
        exploringItems.epsilonDecay = this.epsilonDecay;
        exploringItems.learningRate = this.learningRate;
        exploringItems.targetModelUpdateStep = this.targetModelUpdateStep;
        exploringItems.hiddenLayerCount = this.hiddenLayerCount;
        exploringItems.dqnAgentType = this.dqnAgentType;
        exploringItems.memoryType = this.memoryType;
        exploringItems.rewardType = this.rewardType;
        exploringItems.hiddenLayersNeuronSize = this.hiddenLayersNeuronSize;
        exploringItems.dqnAgent = this.dqnAgent;
        exploringItems.findTheBestGoal = this.findTheBestGoal;
        exploringItems.timeLimit = this.timeLimit;
        exploringItems.lastTime = this.lastTime;


        ////////////////////////////////////////////
        //////////////////////find NACs//////////////
        //////////////////////////////////////////

        Set<QualName> sname= grammermodel.getNames(ResourceKind.RULE);
        Iterator<QualName> it=sname.iterator();
        while(it.hasNext())
        {
            QualName ts=it.next();
            if(ts.equals(QualName.name(ModelCheckingTarget))){
                RuleModel rulemodel=grammermodel.getRuleModel(ts);

                Condition condition=null;

                try {
                    condition=rulemodel.toResource().getCondition();
                } catch (FormatException e) {
                    // do nothing
                    e.printStackTrace();
                }

                Collection<Condition> allcond=condition.getSubConditions();
                Set<RuleEdge> patEdgeSet=condition.getPattern().edgeSet();
                for(RuleEdge re:patEdgeSet){
                    exploringItems.targetGraph_edgeList.add(re);
                    if(!exploringItems.targetGraph_nodeList.contains(re.source()))
                        exploringItems.targetGraph_nodeList.add(re.source());
                    if(!exploringItems.targetGraph_nodeList.contains(re.target()))
                        exploringItems.targetGraph_nodeList.add(re.target());
                }
                for(Condition cond : allcond){
                    ExploringItemRL.NAC nac=exploringItems.getNewNAC();
                    for(RuleEdge re:cond.getPattern().edgeSet()){
                        nac.ruleedgeList.add(re);
                        if(!nac.rulenodeList.contains(re.source()))
                            nac.rulenodeList.add(re.source());
                        if(!nac.rulenodeList.contains(re.target()))
                            nac.rulenodeList.add(re.target());
                    }

                    for(int i=0;i<=nac.rulenodeList.size()-1;i++){
                        RuleNode rn=nac.rulenodeList.get(i);
                        for(RuleEdge re:patEdgeSet){
                            if(re.isLoop() && re.source().equals(rn) && !nac.ruleedgeList.contains(re))
                                nac.ruleedgeList.add(re);
                        }
                    }
                    exploringItems.allNACs.add(nac);
                }
                break;
            }
        }


        //////////////////////////////////////////
        //////////////////////////////////////////

        exploringItems.targetRule=targetRule;
        exploringItems.simulator=simulator;



        ////////////////////////////////////
        ArrayList<QualName> Alltype=new ArrayList<QualName>();

        sname= grammermodel.getNames(ResourceKind.RULE);
        it=sname.iterator();
        while(it.hasNext()){
            QualName ts=it.next();
            RuleModel rulemodel=grammermodel.getRuleModel(ts);
            if(rulemodel.isEnabled()){
                Set<? extends AspectEdge> edgeSet=rulemodel.getSource().edgeSet();

                boolean flag=false;
                for(AspectEdge ae:edgeSet ){
                    if(ae.toString().contains("new:") ||ae.toString().contains("del:") ){
                        flag=true;
                        break;
                    }
                }
                if(!flag){
                    try{
                        if(rulemodel.toResource().getAnchor().size()>0)
                            flag=true;
                    }
                    catch (FormatException e) {
                        // do nothing
                        e.printStackTrace();
                    }
                }

                if(!flag)
                    Alltype.add(ts);
            }
        }

        exploringItems.Alltype=Alltype;

        ////////////////////////////

        exploringItems.isProgressVisible=false;;
        exploringItems.CTLproperty=CTLproperty;

        exploringItems.RulesCount=RulesCount;
        exploringItems.RulesName=RulesName;

        exploringItems.Number_Explored_States=0;
        RLExploreAction rlExploreAction=new RLExploreAction(simulator, false);
        ///////////////////////////
//        exploringItems.dqnAgent.loadWeights("model.zip");
        exploringItems.init = false;
        boolean flag = true;
        long start = System.currentTimeMillis();
        for (int i =0; i<exploringItems.episodes && exploringItems.heuristicResult==null && flag;i++) {
            if (exploringItems.findTheBestGoal && System.currentTimeMillis()>exploringItems.lastTime)
                flag = false;
            if(!callFromHeuGenerator){
                simulator.getModel().resetGTS();
                exploreType=exploringItems.simulator.getModel().getExploreType();
                exploringItems.gts=simulator.getModel().getGTS();
                exploringItems.initialState=exploringItems.gts.startState();
            }else{
                    GTS gts=null;
                try {
                    gts = new GTS(exploringItems.grammer);
                } catch (FormatException e) {
                    // do nothing
                    e.printStackTrace();
                }
                exploringItems.gts=gts;
                exploringItems.initialState=gts.startState();
            }
            rlExploreAction=new RLExploreAction(simulator, false);
            rlExploreAction.explore(exploreType, exploringItems);
            System.err.println("---------------------------- Episode: " + i + " Reward: " + exploringItems.rewards + " ||||||||| Max State: " + exploringItems.Number_Explored_States + "------------------------");
            if (exploringItems.heuristicResult!=null) {
                System.err.println("---------------------------- Found On State: " + exploringItems.lastStateInReachability +  "-------------------------");
//				exploringItems.dqnAgent.saveWeights("model.zip");
				if (exploringItems.findTheBestGoal && System.currentTimeMillis()<exploringItems.lastTime) {
                    long end = System.currentTimeMillis();
                    ExploringItemRL.GoalState goalState = exploringItems.getNewGoalState();
				    goalState.foundTime = end - start;
				    goalState.witnessLength = exploringItems.First_Found_Reach_depth;
				    goalState.exploredstate = exploringItems.Number_Explored_States;
                    exploringItems.Number_Explored_States = 0;
				    exploringItems.goalStatesInfo.add(goalState);
                    exploringItems.heuristicResult=null;
                    start = System.currentTimeMillis();
                }
            }
            exploringItems.rewards = 0;
            if (exploringItems.fromMaxStep <= exploringItems.toMaxStep - exploringItems.maxStepIncrement)
                exploringItems.fromMaxStep += exploringItems.maxStepIncrement;
            exploringItems.allActionsUntilNow.clear();
        }
        if (exploringItems.goalStatesInfo.size() > 0) {
            exploringItems.heuristicResult = "reachability";
        }
        Number_Explored_States=exploringItems.Number_Explored_States;
        First_Found_Dead_depth=exploringItems.First_Found_Reach_depth;


        if(!callFromHeuGenerator==true)
            if(exploringItems.heuristicResult==null)
                simulator.getModel().resetGTS();



        ////////////////////////////////
        ///////////////////////////////
        if(callFromHeuGenerator==true){
            if(exploringItems.heuristicResult.equals("reachability")){
                return "The property is verified."+" Target state found in depth:"+ exploringItems.First_Found_Reach_depth +" The number of explored states:"+exploringItems.Number_Explored_States +" ";
            }
            else
                return "The property is not verified.";
        }
        ////////////////////////////////
        ///////////////////////////////


        if(exploringItems.heuristicResult==null)
            exploringItems.heuristicResult="noreachability";
        else
            exploringItems.heuristicResult="reachability"+"_"+exploringItems.lastStateInReachability.toString();

        return "";
    }



    public Simulator simulator;
    public String HostGraphName;
    public String ModelCheckingType;
    public String ModelCheckingTarget;
    public String CTLproperty="";
    public long Number_Explored_States;
    public long First_Found_Dead_depth; //The first found deadlock depth
    public ArrayList<QualName> Alltype;

    /**
     * Save all paths from final state's to initial state.
     */

    public ExploringItemRL exploringItems;
    public int RulesCount;
    public int episodes;
    public int rewards;
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
    public int timeLimit;
    public long lastTime;
    public boolean memoryType;
    public boolean findTheBestGoal;
    public int[] hiddenLayersNeuronSize;
    public float discountFactor;
    public float epsilonMin;
    public float epsilonDecay;
    public float learningRate;
    public static final int DEEP_Q_NETWORK_AGENT = 1;
    public static final int DOUBLE_DEEP_Q_NETWORK_AGENT = 2;
    public static final boolean SIMPLE_EXPERIENCE_REPLAY_MEMORY = false;
    public static final boolean PRIORITIZED_EXPERIENCE_REPLAY_MEMORY = true;
    public RLAgent dqnAgent;
    public Boolean callFromHeuGenerator=false;

}