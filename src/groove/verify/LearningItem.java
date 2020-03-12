package groove.verify;


import java.util.ArrayList;

import groove.grammar.QualName;
import  groove.grammar.Rule;
import groove.gui.Simulator;
import groove.lts.GraphState;


/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */

public class LearningItem {
	
	public LearningItem(){
		allRules=new ArrayList<Rule>();
		allRulesNames=new ArrayList<String>();
		allRulesNamesNorepeat=new  ArrayList<String>();
		
		allcurStatesNames=new ArrayList<String>();
		allcurStates=new ArrayList<GraphState>();
		
		allprevStatesNames=new ArrayList<String>();
		allprevStates=new ArrayList<GraphState>();
		
		Alltype=new ArrayList<QualName>();
		allpath_From_Max_To_s0=new ArrayList<String>();
		CopyOfallpath_From_Max_To_s0=new ArrayList<String>();
		ExportedpatternNorepeat=new ArrayList<String>();
	
		C1_Items=new ArrayList<Item>(); 
		CK_Items=new ArrayList<Item>();
		Ctemp_Items=new ArrayList<Item>();
		Cresp_Items=new ArrayList<Item>();
		pathLeadCycleInLargeModel=new ArrayList<StateRule>();
		Number_Explored_States=0;
		
	}
			
	
	public Item getNewItem(){
		return new Item();
	}
	
	
	
	public GraphState resultState;
	public String resultStateName;
	
	public ArrayList<StateRule> pathLeadCycleInLargeModel;  //this path used for the refutation of liveness by cycle
	
	// for i:  allprevStates[i]--allRules[i]-->allcurStates[i];
	
	public ArrayList<String> allcurStatesNames;
	public ArrayList<GraphState> allcurStates;
		
	public ArrayList<String>  allprevStatesNames;
	public ArrayList<GraphState> allprevStates;
	
	public ArrayList<Rule> allRules;
	public ArrayList<String> allRulesNames;
	
	public ArrayList<String> allRulesNamesNorepeat;
	
	public Simulator simulator;
	public String heuristicResult;
	public long Number_Explored_States;
	
	public ArrayList<QualName> Alltype;
	
	///Is the progress of exploring is visible 
	public boolean isProgressVisible;
	
	public String Exportedpattern="";
	public ArrayList<String> ExportedpatternNorepeat;
	
	/**
     * In final, all paths are calculated from S0 to Max  
     */
	public ArrayList<String>  allpath_From_Max_To_s0;
	public ArrayList<String>  CopyOfallpath_From_Max_To_s0;
	
	public ArrayList<Item> C1_Items;
	public ArrayList<Item> CK_Items;
	public ArrayList<Item> Ctemp_Items;
	public ArrayList<Item> Cresp_Items;
	
	public int startIndexofCycle;
	 
	public class StateRule{
		public GraphState state;
		public Rule rule;
	}
	
	public StateRule getNewStateRule(){
		return new StateRule();
	}
	
	public class Item{
		public Item(){
			rules="";
			support=0;
		}
		public String rules;
		public double support;
	}
	
	
	
}
