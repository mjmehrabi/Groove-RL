package groove.verify;


import java.util.ArrayList;
import java.util.List;






import groove.explore.ExploreType;
import groove.grammar.Grammar;
import groove.grammar.QualName;
import groove.grammar.model.GrammarModel;
import groove.grammar.rule.RuleEdge;
import groove.grammar.rule.RuleNode;
import groove.gui.Simulator;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.lts.MatchResult;
import groove.verify.Exploringinfo;

/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */

public class ExploringGaBayesNet {
	
	public ExploringGaBayesNet(){
		population=new ArrayList<Chromosome>();
		
		baysNet=new BaysianNetwork();
		
		allinfo=new ArrayList<Exploringinfo>(); 
		
		Alltype=new ArrayList<QualName>();
		RulesName=new ArrayList<QualName>();
		
		tempStates=new ArrayList<TempState>();
		allNACs=new ArrayList<NAC>();
		
		AllInfoGeneration=new ArrayList<InfoGeneration>();
		
		goalStatesInfo=new ArrayList<GoalState>();
	}
			

	public Chromosome getNewChromosome(){
		return new Chromosome();
	}
	
	public GTS gts=null;
	
	public boolean callFromHeuGenerator=false;
	
	public GraphState initialState=null;
	public Grammar grammer=null;
	public GrammarModel grammermodel=null;
	public ExploreType exploreType=null;
	
	public ArrayList<QualName> Alltype;
	
	public GraphState State_Max_EQU;
	/*
	 * beforstate is s0
	 */
	public GraphState  beforeState;
	
	public GraphState lastStateInReachability;
	public long Number_Explored_States;
	public long First_Found_Dead_depth; //The first found deadlock depth
	public long First_Found_Dead_Rep;  //The first deadlock is found after how many repetitions of BOA
	public long Call_Number_Fitness;  //The call number of fitness function
	public long RunningTime_AllFitnessFuncs; //The running time of all fitness function calls
	public String HostGraphName="";
	
	public int Max_EQU;
	public int RulesCount;
	public ArrayList<QualName> RulesName;
	public String targetRule="";
	public String ModelCheckingTarget="";
	
	
	//public Set<? extends AspectEdge> targetGraph_edgeSet;
	//public Set<? extends AspectNode> targetGraph_nodeSet;
	
	public ArrayList<RuleEdge> targetGraph_edgeList=new ArrayList<RuleEdge>();
	public ArrayList<RuleNode> targetGraph_nodeList=new ArrayList<RuleNode>();

	
	
	public ArrayList<Exploringinfo> allinfo; 
	

	
	public Simulator simulator;
	
	public String heuristicResult;
	
	
	public String WhatStep="";
	
	///Is the progress of exploring is visible 
	public boolean isProgressVisible;
	
	public boolean isExportBayesianProbs=false;
	public boolean isExportInfoGeneartions=false;
	
	public String SelectionType="TRUNC";
	public String GAType="GA";
	public String BOAType="BOA";
	public ArrayList<Chromosome> population;
	public ArrayList<InfoGeneration> AllInfoGeneration;
	public int CountOFpopulation;
	public int Iterations;
	public int DepthOfSearch;
	public double MutationRate;
	public double CrossOverRate;
	public int totalFitness=0;
	public int maxValueInAllChromosomes=0;
	public int chroIndex=0;
	public int chroCountForLearnBayesNet=0;
	public int chroCountReplaceBySampling=0;
	public int chroIndexCounterExamlpe=-1;
	public long OPTValueOfFitness=0;
	
	
	public int timeLimit=0;
	public boolean isContinue=false;
	public long lastTime=0;
	
	
	public class Chromosome{
		public Chromosome(){
			fitness=0;
			cumAvgFitness=0;
			genes=new ArrayList<Integer>();
			ruleNames=new ArrayList<String>();
			states=new ArrayList<GraphState>();
		}
		public ArrayList<Integer> genes;
		public ArrayList<String> ruleNames;
		public ArrayList<GraphState> states;
		public GraphState lastState;
        public int fitness;   //   fitness_number_invisible_variables
        public double cumAvgFitness; //Cumulative of average of fitness value
	}
	public InfoGeneration getNewInfoGeneration(){
		return new InfoGeneration();
	}
	public class InfoGeneration{
		public InfoGeneration(){
			num=0;
			MinValueOfFitness=0;
			AvgValueOfFitness=0;
			MaxValueOfFitness=0;
		}
		public int num; //The number of generation
		public long MinValueOfFitness;
		public long AvgValueOfFitness;
		public long MaxValueOfFitness;
	}
	
	public BaysianNetwork baysNet;
	
	public class TempState{
		public TempState(){
			allNextStates=new  ArrayList<GraphState>();
			allRuleNames=new  ArrayList<String>();
		}
		public GraphState curstate;
		public List<MatchResult> matches;
		public ArrayList<GraphState> allNextStates;
		public ArrayList<String> allRuleNames;

	}
	
	public TempState getNewTempState(){
		return new TempState();
	}
	
	public ArrayList<TempState> tempStates;
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
	
	public ArrayList<GoalState> goalStatesInfo;
	public GoalState getNewGoalState(){
		return new GoalState();
	}
	public class GoalState{
		public long foundTime=0;
		public long witnessLength=0;
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


