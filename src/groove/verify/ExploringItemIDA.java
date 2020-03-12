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



public class ExploringItemIDA {
	
	public ExploringItemIDA(){
		allRules=new ArrayList<Rule>();
		allRulesNames=new ArrayList<String>();
		allcurStatesNames=new ArrayList<String>();
		allcurStates=new ArrayList<GraphState>();
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
		allPaths=new ArrayList<Path>();
		allPathsTemp=new ArrayList<Path>();
		
		RulesName=new ArrayList<QualName>();
		allinfo=new ArrayList<Exploringinfo>(); 
		
		Alltype=new ArrayList<QualName>();
		allNACs=new ArrayList<NAC>();
		tempStates=new ArrayList<TempState>();
		Number_Explored_States=0;
		witORcountPath=getNewPath();
		
	}
			
	
	
	
	
	// for i:  allprevStates[i]--allRules[i]-->allcurStates[i];
	
	public ArrayList<String> allcurStatesNames;
	public ArrayList<GraphState> allcurStates;
	
	public int repeat=0;
	
	public ArrayList<Rule> allRules;
	public ArrayList<String> allRulesNames;
	public ArrayList<Integer> allcurdepth;
	public ArrayList<Integer>  allEQU_Count;
	public ArrayList<String>  allnextStatesNames;
	public ArrayList<GraphState> allnextStates;

	
	public String targetRule="deadlock";
	public int maxNumberOfStates=0; 
	
	public ArrayList<RuleEdge> targetGraph_edgeList=new ArrayList<RuleEdge>();
	public ArrayList<RuleNode> targetGraph_nodeList=new ArrayList<RuleNode>();
	
	/**
	 * This path specifies a witness or a counterexample
	 */
	public Path witORcountPath;

	public String ModelCheckingTarget="";
	public Integer maxDepthOfSearch=100;
	
	public ArrayList<String> allcurStatesNames_Extra;
	public ArrayList<GraphState> allcurStates_Extra;
	public ArrayList<Rule> allRules_Extra;
	public ArrayList<String> allRulesNames_Extra;
	public ArrayList<Integer> allcurdepth_Extra;
	public ArrayList<Integer>  allEQU_Count_Extra;
	public ArrayList<String>  allnextStatesNames_Extra;
	public ArrayList<GraphState> allnextStates_Extra;
	
	public String typeOfAlg="A*";   //A*   IDA*   BeamSearch
	public int Beamwidth=10;
	public int RulesCount;
	public ArrayList<QualName> RulesName;
	/**
	 * ::::  HEU_Blocked_Rules_In_Path  ::::
	 * <li>path=s0s1...sf leading to the state sf
	 * <li>f(sf)=numblocked+1/(1+pathlen)    
	 * <li>numblocked=sum(RulesCount-out(si))   0<=i<=f
	 * <li>out(si)=The number of enable rules on si
	 * <Li>::::  HEU_Blocked_Rules_In_LastState  ::::
	 * <li>path=s0s1...sf leading to the state sf
	 * <li>f(sf)=numblocked+1/(1+pathlen)    
	 * <li>numblocked=RulesCount-out(sf)   
	 * <li>out(si)=The number of enable rules on si
	 */
	public String typeOfHeuristic="HEU_BLKRULESPATH"; //by default: HEU_Blocked_Rules_Path
	
	public ArrayList<Exploringinfo> allinfo; 
	
	public Simulator simulator;
	
	public GTS gts=null;
	
	public boolean callFromHeuGenerator=false;
	
	public GraphState initialState=null;
	public Grammar grammer=null;
	public GrammarModel grammermodel=null;
	public ExploreType exploreType=null;
	
	public String heuristicResult;
	
	public GTS gtsLearning;
	

	
	///Is the progress of exploring is visible 
	public boolean isProgressVisible;
	public String CTLproperty="deadlock";
	
	public GraphState lastStateInReachability;
	public long Number_Explored_States;
	public long All_Number_Explored_States;
	public long First_Found_Dead_depth; //The first found deadlock depth
	
	public ArrayList<QualName> Alltype;
	
	
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
	
	
	public Path getNewPath(){
		return new Path();
	}
	
	public ArrayList<Path> allPaths;
	public ArrayList<Path> allPathsTemp;
	
	public class Path{
		public Path(){
			items=new ArrayList<StateRule>();
		}
		public ArrayList<StateRule> items;
		public double f=0;  //f(path)=numblocked+1/(1+pathlen) 
	}
	
	
	public class StateRule{   //rule-->state
		public GraphState state;
		public String ruleName;
		public int outTransSize=0;
	}
	
	public StateRule getNewStateRule(){
		return new StateRule();
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


