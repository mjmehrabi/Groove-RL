package groove.verify;


import java.util.ArrayList;

import groove.grammar.QualName;
import  groove.grammar.Rule;
import groove.grammar.aspect.AspectEdge;
import groove.grammar.aspect.AspectNode;
import groove.grammar.type.TypeLabel;
import groove.lts.GraphState;



public class ExploringAuto {
	
	public ExploringAuto(){
		allCluster=new ArrayList<Cluster>();
		pathLeadCycle=new ArrayList<StateRule>();
		checkedStates=new ArrayList<GraphState>();
		allPartialPaths=new ArrayList<PartialPath>();
		Alltype=new ArrayList<QualName>();
	}
	
	public ArrayList<Cluster> allCluster;   //Host graph entities are divided into different clusters!
	
	public ArrayList<StateRule> pathLeadCycle;  //this path used for the refutation of liveness by cycle
	public int startIndexofCycle;  //this variable specifies the index of a Cycle Starting 
	public Boolean isFindpathLeadCycle=false;
	public ArrayList<GraphState> checkedStates;
	public ArrayList<PartialPath> allPartialPaths;
	public int maxRepetitionOfGenSmallerModel=10;
	public Boolean isReachToCorrectSmaller=false;
	public ArrayList<QualName> Alltype;
	
	
	public class PartialPath{
		public PartialPath(){
			pathS0_to_CurState=new ArrayList<StateRule>();
		}
		public ArrayList<StateRule> pathS0_to_CurState;
		public StateRule curStateRule;
	}
	
	public PartialPath getNewPartialPath(){
		return new PartialPath();
	}
	
	public class StateRule{
		public GraphState state;
		public Rule rule;
	}
	
	public StateRule getNewStateRule(){
		return new StateRule();
	}
	
	public class Cluster{
		public Cluster(){
			allItem=new ArrayList<Item>();
			allTypesOfCluster=new ArrayList<TypeLabel>();
			CopyOfallTypesOfCluster=new ArrayList<TypeLabel>();
		}
		public ArrayList<Item> allItem;
		public Boolean isAllTypeEquall=true;
		public TypeLabel typeOfCluster;
		public ArrayList<TypeLabel> allTypesOfCluster;
		public ArrayList<TypeLabel> CopyOfallTypesOfCluster;
		
		
	}
	public Cluster getNewCluster(){
		return new Cluster();
	}
	
	public class Item{
		public Item(){
			allAsEdgeSelf=new  ArrayList<AspectEdge>();
			
			allAsEdgeIn=new  ArrayList<AspectEdge>();
			allAsEdgeInISvisited=new  ArrayList<Boolean>();
			
			allAsEdgeOut=new  ArrayList<AspectEdge>();
			allAsEdgeOutISvisited=new  ArrayList<Boolean>();
		}
		public AspectNode Asnode;
		public ArrayList<AspectEdge> allAsEdgeSelf;  //specify attributes
		public ArrayList<AspectEdge> allAsEdgeIn;    //specify input edges
		public ArrayList<Boolean> allAsEdgeInISvisited;
		public ArrayList<AspectEdge> allAsEdgeOut; //specify output edges
		public ArrayList<Boolean> allAsEdgeOutISvisited;
		public Boolean AsnodeIsvisited;
	}
	public Item getNewItem(){
		return new Item();
	}
	
}


