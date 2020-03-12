package groove.verify;


import java.util.ArrayList;
import java.util.List;




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

/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */

public class ExploringItem {
	
	public ExploringItem(){
		allRules=new ArrayList<Rule>();
		allRulesNames=new ArrayList<String>();
		allcurStatesNames=new ArrayList<String>();
		allcurStates=new ArrayList<GraphState>();
		allcurStatesOutDegree=new ArrayList<Integer>();
		allEQU_Count=new ArrayList<Integer>();
		allnextStatesNames=new ArrayList<String>();
		allnextStates=new ArrayList<GraphState>();
		allcurdepth=new ArrayList<Integer>();
		
		allRules_Extra=new ArrayList<Rule>();
		allRulesNames_Extra=new ArrayList<String>();
		allcurStatesNames_Extra=new ArrayList<String>();
		allcurStates_Extra=new ArrayList<GraphState>();
		allEQU_Count_Extra=new ArrayList<Integer>();
		allnextStatesNames_Extra=new ArrayList<String>();
		allnextStates_Extra=new ArrayList<GraphState>();
		allcurdepth_Extra=new ArrayList<Integer>();
		
		appliedPromisSates=new ArrayList<GraphState>();
		
		//allprevStatesNames=new ArrayList<String>();
		//allprevStates=new ArrayList<GraphState>();
		
		
		allinfo=new ArrayList<Exploringinfo>();
		
		allpath_From_S0_To_Max=new ArrayList<String>();
		orig_allpath_From_S0_To_Max=new ArrayList<String>();
		
		allpath_From_S0_To_Max_fitness=new ArrayList<Integer>();
		allknowledge=new ArrayList<Exploringknowledge>();
		
		allcurStates_IsfindEQU=new ArrayList<Boolean>();
	
		RulesName=new ArrayList<QualName>();
		
		
		baysNet=new BaysianNetwork();
		allNACs=new ArrayList<NAC>();
		
		Alltype=new ArrayList<QualName>();
		
		C1_Items=new ArrayList<Item>(); 
		CK_Items=new ArrayList<Item>();
		Ctemp_Items=new ArrayList<Item>();
		Cresp_Items=new ArrayList<Item>();
		ExportedpatternNorepeat=new ArrayList<String>();
		
		pathLeadCycle=new ArrayList<StateRule>();
		tempStates=new ArrayList<TempState>();
		Number_Explored_States=0;
		
	}
			
	
	
	
	
	// for i:  allprevStates[i]--allRules[i]-->allcurStates[i];
	
	public ArrayList<String> allcurStatesNames;
	public ArrayList<GraphState> allcurStates;
	/**
     * This is used for deadlock detection
     */
	public ArrayList<Integer> allcurStatesOutDegree;
	
	public int repeat=0;
	
	public ArrayList<Rule> allRules;
	public ArrayList<String> allRulesNames;
	public ArrayList<Integer> allcurdepth;
	public ArrayList<Integer>  allEQU_Count;
	public ArrayList<String>  allnextStatesNames;
	public ArrayList<GraphState> allnextStates;
	public ArrayList<Boolean> allcurStates_IsfindEQU;
	
	public int maxNumberOfStates=0; 
	 /**
     * minimum support percentage 
     */
	public Double minsup=0.6;
	public Integer maxDepth=500;
	
	public ArrayList<String> allcurStatesNames_Extra;
	public ArrayList<GraphState> allcurStates_Extra;
	public ArrayList<Rule> allRules_Extra;
	public ArrayList<String> allRulesNames_Extra;
	public ArrayList<Integer> allcurdepth_Extra;
	public ArrayList<Integer>  allEQU_Count_Extra;
	public ArrayList<String>  allnextStatesNames_Extra;
	public ArrayList<GraphState> allnextStates_Extra;
	
	public int RulesCount;
	public ArrayList<QualName> RulesName;
	
	public String typeOfLearn="BN"; //by default: Bayesian Network
	public Boolean isFirstStep=false;
	
	public GraphState State_Max_EQU;
	/*
	 * beforstate is s0
	 */
	public GraphState  beforeState;
	
	public int Max_EQU;
	
	public ArrayList<GraphState> appliedPromisSates;
	
	/**
     * In final, all paths are calculated from S0 to Max  
     */
	
	public ArrayList<String>  allpath_From_S0_To_Max;
	public ArrayList<String>  orig_allpath_From_S0_To_Max;
	
	public ArrayList<Integer>  allpath_From_S0_To_Max_fitness; //for deadlock sum of out transitions of each state in path
	/**
	 * This number determines the maximum number of the generated paths from s0 to max
	 */
	public Integer maxNum_allPathFs0TMax=100;  
	
	public ArrayList<StateRule> pathLeadCycle;  //this path used for the refutation of liveness by cycle
	
	// for i:  allprevStates[i]--allRules[i]-->allcurStates[i];
	
	
	public String targetRule="";
	
	public ArrayList<RuleEdge> targetGraph_edgeList=new ArrayList<RuleEdge>();
	public ArrayList<RuleNode> targetGraph_nodeList=new ArrayList<RuleNode>();
	
	
	public ArrayList<Exploringinfo> allinfo; 
	
	public ArrayList<Exploringknowledge> allknowledge;
	
	public Simulator simulator;
	
	public GTS gts=null;
	
	public boolean callFromHeuGenerator=false;
	
	public GraphState initialState=null;
	public Grammar grammer=null;
	public GrammarModel grammermodel=null;
	public ExploreType exploreType=null;
	
	
	public String heuristicResult;
	
	public GTS gtsLearning;
	
	public int maxDepthOfSearch;
	
	public int minDepthOfSearch;
	
	///Is the progress of exploring is visible 
	public boolean isProgressVisible;
	public String CTLproperty="reachability";
	
	public GraphState lastStateInReachability;
	public long Number_Explored_States;
	public long All_Number_Explored_States;
	public long First_Found_Dead_depth; //The first found deadlock depth
	
	public ArrayList<QualName> Alltype;
	
	public BaysianNetwork baysNet;
	
	public String Exportedpattern="";
	public ArrayList<String> ExportedpatternNorepeat;

	public ArrayList<Item> C1_Items;
	public ArrayList<Item> CK_Items;
	public ArrayList<Item> Ctemp_Items;
	public ArrayList<Item> Cresp_Items;
	
	
	public class StateRule{
		public GraphState state;
		public Rule rule;
	}
	
	public StateRule getNewStateRule(){
		return new StateRule();
	}
	
	public Item getNewItem(){
		return new Item();
	}
	
	public class Item{
		public Item(){
			rules="";
			support=0;
		}
		public String rules;
		public double support;
	}
	
	
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


