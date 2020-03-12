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
public class ExploringItemPSO {
	
	public ExploringItemPSO(){
		allinfo=new ArrayList<Exploringinfo>();
		
		population=new ArrayList<Particle>();
		
		Alltype=new ArrayList<QualName>();
		RulesName=new ArrayList<QualName>();
		tempStates=new ArrayList<TempState>();
		allNACs=new ArrayList<NAC>();
	}
			

	public Particle getNewParticle(){
		return new Particle();
	}
	
	public ArrayList<QualName> Alltype;
	
	public GraphState State_Max_EQU;
	/*
	 * beforstate is s0
	 */
	public GraphState  beforeState;
	
	public int Max_EQU;
	public int RulesCount;
	public ArrayList<QualName> RulesName;
	
	public GTS gts=null;
	
	public boolean callFromHeuGenerator=false;
	public String targetRule="";
	public String ModelCheckingTarget="";
	
	//public Set<? extends AspectEdge> targetGraph_edgeSet;
	//public Set<? extends AspectNode> targetGraph_nodeSet;
	
	public ArrayList<RuleEdge> targetGraph_edgeList=new ArrayList<RuleEdge>();
	public ArrayList<RuleNode> targetGraph_nodeList=new ArrayList<RuleNode>();

	
	
	public ArrayList<Exploringinfo> allinfo; 
	
	public String psoType="PSO";  //PSO  PSO-GSA
	public GraphState initialState=null;
	public Grammar grammer=null;
	public GrammarModel grammermodel=null;
	public ExploreType exploreType=null;
	
	public GraphState lastStateInReachability;
	public long Number_Explored_States;
	public long First_Found_Dead_depth; //The first found deadlock depth
	public long First_Found_Dead_Rep;  //The first deadlock is found after how many repetitions of BOA
	public long Call_Number_Fitness;  //The call number of fitness function
	
	public int partIndexCounterExamlpe=-1;
	public Simulator simulator;
	
	public String heuristicResult;
	public String WhatStep="";
	
	///Is the progress of exploring is visible 
	public boolean isProgressVisible;
	public String CTLproperty;
	/**
	 * A Swarm of Particles  
	 */
	public ArrayList<Particle> population;    
	/**
	 * SWARM_SIZE
	 */
	public int CountOFpopulation=30;         
	public int Iterations=100;
	/**
	 * PROBLEM_DIMENSION
	 */
	public int DepthOfSearch;                
	public double C1=2.0;
	public double C2=2.0;
	public double W=0.8;
	public int totalFitness=0;
	public int maxValueInAllParticles=0;
	public int partIndex=0;
	public int K;
	public double G;

	public int indexOFpartBestFit;
	public int indexOFpartWorstFit;
	
	
	/**
	 * The best fitness value for each particle until now
	 */
	public double[] pBest;    //new double[SWARM_SIZE];
	/**
	 * The best genes for each particle until now
	 */	
	public Location[] pBestLocation;   //new Location[SWARM_SIZE];
	/**
	 * The best fitness value for all particles until now
	 */
	public double gBest;
	/**
	 * The best genes for all particles until now
	 */
	public Location gBestLocation;
	
	public class Particle{
		public Particle(){
			fitness=0;
			genes=new ArrayList<Integer>();
			ruleNames=new ArrayList<String>();
			states=new ArrayList<GraphState>();
			location=new Location();
			velocity=new Velocity();
			force=new Force();
			acceleration=new Acceleration();
		}
		/**
		 * locations of particles
		 */
		public ArrayList<Integer> genes;  
		public ArrayList<String> ruleNames;
		public ArrayList<GraphState> states;
		public GraphState lastState;
        public double fitness;   
        public Location location;    //genes 
        public Velocity velocity;    //velocity of genes
        public Acceleration acceleration; 
        public Force force;
        public double mass; 
	}
	public Location getNewLocation(){
		return new Location();
	}
	public class Location{
		public int[] loc;   //genes
	}
	
	public Velocity getNewVelocity(){
		return new Velocity();
	}
	
	public class Velocity{
		public int[] vel;
	}
	
	public Acceleration getNewAcceleration(){
		return new Acceleration();
	}
	
	public class Acceleration{
		public double[] acc;
	}
	public Force getNewForce(){
		return new Force();
	}
	
	public class Force{
		public int[] frc;
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


