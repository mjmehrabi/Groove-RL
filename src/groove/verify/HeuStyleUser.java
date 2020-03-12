package groove.verify;

import groove.grammar.QualName;
import groove.grammar.Rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import groove.eclat.AlgoEclat;
import groove.eclat.TransactionDatabase;
import groove.explore.Exploration;
import groove.explore.ExploreType;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.model.RuleModel;
import groove.gui.Simulator;
import groove.gui.SimulatorModel;
import groove.gui.action.ExploreAction;
import groove.gui.action.HeuStyleExploreAction;

import groove.gui.display.DisplayKind;
import groove.gui.display.LTSDisplay;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.lts.GraphTransition;
import groove.util.parse.FormatException;
import groove.verify.ExploringAuto.StateRule;
import groove.verify.LearningItem.Item;

/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */

public class HeuStyleUser{
	public HeuStyleUser(){
		ALearningItems=new ArrayList<LearningItem>();
	}
	
	
	public void EnableSelectedHostGraph(){
        try {
            simulator.getModel().doEnableUniquely(ResourceKind.HOST,QualName.name(HostGraphName));
        } catch (IOException exc) {
        	System.err.println("Error during %s enabling" +ResourceKind.HOST.getDescription());
        }
        
	}
	public void Explore(){
		ALearningItems.clear();
		LearningItem learnitem=new LearningItem();
		ALearningItems.add(learnitem);
		ALearningItems.get(0).isProgressVisible=false;
		
		
		simulator.getModel().resetGTS();  //Creates a fresh GTS and fires an update event.
		GTS gts = simulator.getModel().getGTS();
		SimulatorModel simulatorModel = simulator.getModel();
		ExploreType exploreType=simulatorModel.getExploreType();	
		HeuStyleExploreAction heuExploreAction=new HeuStyleExploreAction(simulator, false);
		heuExploreAction.exploreSmallModel(simulatorModel.getState(), exploreType);
		
		ALearningItems.get(0).isProgressVisible=false;
	}
	
	
	public void MakeKnowlegeBase(){
		//try{

		ALearningItems.clear();
		GTS gts = simulator.getModel().getGTS();
		Set<? extends GraphState> nodeset=gts.nodeSet();
		Set<? extends GraphTransition> edgeset=gts.edgeSet();
		
		
		GrammarModel grammermodel=simulator.getModel().getGrammar();
		
		initialState=gts.startState();
		initialStateName=gts.startState().toString();
		
		Collection<GraphState> resultstates;
		//////////////////////////////////////////////////////
		//////////////////////////////////////////////////////
		///////////For All data mining algorithms//////////
		
		 Set <? extends Rule>  ruleset=gts.getGrammar().getAllRules();////
		 ArrayList<Rule> allRules=new ArrayList<Rule>();
		 ArrayList<String> allRuleNames=new ArrayList<String>();
		 
        for(Rule r:ruleset){
        	allRules.add(r);
        	allRuleNames.add(r.getQualName().toString());
        }
		
        //////////////////////////////////////////////////////
        //////////////////////////////////////////////////////
		
		////////////////////////////////////////////////////////
		if(ModelCheckingType.equals("RefuteLivenessByCycle")){
			nodeset=gts.nodeSet();
			edgeset=gts.edgeSet();
			
			ExploringAuto exploringAuto=new ExploringAuto();
			
			exploringAuto.isFindpathLeadCycle=false;
			exploringAuto.checkedStates.add(initialState);
			ArrayList<StateRule> pathLeadCycle=new ArrayList<StateRule>();
			dfs_pathCycle(null, null,initialState,nodeset,edgeset,exploringAuto);
			
			if(exploringAuto.isFindpathLeadCycle){
				LearningItem learnitem=new LearningItem();
				learnitem.startIndexofCycle=exploringAuto.startIndexofCycle;
				int x=0;
				for(int i=1;i<=exploringAuto.pathLeadCycle.size()-1;i++){
					String s=exploringAuto.pathLeadCycle.get(i).rule.getQualName().toString();
					if(learnitem.ExportedpatternNorepeat.size()==0)
						learnitem.ExportedpatternNorepeat.add(s);
					else if(!learnitem.ExportedpatternNorepeat.get(learnitem.ExportedpatternNorepeat.size()-1).equals(s))
						learnitem.ExportedpatternNorepeat.add(s);
					else if(i<learnitem.startIndexofCycle)
						x++;
				}
				learnitem.startIndexofCycle--;  ///for s0,rule=null
				learnitem.startIndexofCycle-=x;
				
				/////extend the learnitem.ExportedpatternNorepeat 
				int p=learnitem.ExportedpatternNorepeat.size()-1;
				for(int i=1;i<=10;i++){
					for(int j=learnitem.startIndexofCycle;j<=p;j++){
						String s=learnitem.ExportedpatternNorepeat.get(j);
						learnitem.ExportedpatternNorepeat.add(s);
					}
				}
				
				ALearningItems.add(learnitem);
			}
		
			return;
		}
		////////////////////////////////////////////////////////
		
		if(ModelCheckingType.equals("DeadLock")){
			resultstates=gts.getFinalStates();
			for(GraphState gs :resultstates){
				LearningItem learnitem=new LearningItem();
				learnitem.resultState=gs;
				learnitem.resultStateName=gs.toString();
				ALearningItems.add(learnitem);
			}
		}else{
			resultstates=FindTargetStates(edgeset);
			for(GraphState gs :resultstates){
				LearningItem learnitem=new LearningItem();
				learnitem.resultState=gs;
				learnitem.resultStateName=gs.toString();
				ALearningItems.add(learnitem);
			}
		}
			
		for(int x=0;x<=ALearningItems.size()-1;x++){
			GraphState resultState=null,curState=null,prevState=null;
			String ruleName=null;
			Rule rule=null;
			LearningItem learnitem=ALearningItems.get(x);
			resultState=learnitem.resultState;
			curState=resultState;
			prevState=findPrevState(grammermodel,nodeset, curState);
			rule=ruleHelp;
			ruleName=rule.getQualName().toString();
			if(prevState!=null && !prevState.equals(initialState)){
				do{
					learnitem.allRules.add(rule);
					learnitem.allRulesNames.add(ruleName);
					learnitem.allcurStates.add(curState);
					learnitem.allcurStatesNames.add(curState.toString());
					learnitem.allprevStates.add(prevState);
					learnitem.allprevStatesNames.add(prevState.toString());
					curState=prevState;
					prevState=findPrevState(grammermodel,nodeset, curState);
					rule=ruleHelp;
					ruleName=rule.getQualName().toString();
					
				}while(prevState!=null &&  !prevState.equals(initialState));
				if(prevState!=null){
					learnitem.allRules.add(rule);
					learnitem.allRulesNames.add(ruleName);
					learnitem.allcurStates.add(curState);
					learnitem.allcurStatesNames.add(curState.toString());
					learnitem.allprevStates.add(prevState);
					learnitem.allprevStatesNames.add(prevState.toString());
				}
			}else if(prevState!=null && prevState.equals(initialState)){
				learnitem.allRules.add(rule);
				learnitem.allRulesNames.add(ruleName);
				learnitem.allcurStates.add(curState);
				learnitem.allcurStatesNames.add(curState.toString());
				learnitem.allprevStates.add(prevState);
				learnitem.allprevStatesNames.add(prevState.toString());
			}
			
			dfs(resultState, "", learnitem, grammermodel, nodeset);
			
			////remove all extra rules
			for(int i=0;i<=learnitem.allpath_From_Max_To_s0.size()-1;i++){
				String[] s=learnitem.allpath_From_Max_To_s0.get(i).split(",");
				String t="";
				for(int w=0;w<=s.length-1;w++){
					if(!t.contains(s[w])){
						t+=","+s[w];
					}
				}
				learnitem.allpath_From_Max_To_s0.set(i, t.substring(1));
			}
					
			//////////////////////
			learnitem.CopyOfallpath_From_Max_To_s0=(ArrayList<String>) learnitem.allpath_From_Max_To_s0.clone();
			switch (DataMiningType){
			case "Apriori":
				FindFreqPatt_Apriori(learnitem,0.1);
				break;
			case "FpGrowth":
				FPgrowthApp FPGApp=new FPgrowthApp(learnitem,0.1,allRules,allRuleNames);
				FPGApp.RunFPgrowthAPP(learnitem);
				break;
			case "Eclat":
				FindFreqPatt_ECLAT(learnitem,0.1);
				break;
			case "Fin":
				 FIN fin = new FIN();
				 fin.runAlgorithm(learnitem, 0.1);
			default:
				FindFreqPatt_Apriori(learnitem,0.1);
			}
			/////////////////////
			
			learnitem.allpath_From_Max_To_s0=(ArrayList<String>) learnitem.CopyOfallpath_From_Max_To_s0.clone();
			
			
			String[] s=learnitem.Exportedpattern.split(",");
			
			learnitem.ExportedpatternNorepeat.clear();
			for(int w=0;w<=s.length-1;w++){
				if(!learnitem.ExportedpatternNorepeat.contains(s[w])){
					learnitem.ExportedpatternNorepeat.add(s[w]);
				}
			}
			
			
			
			//reorder the items of learnitem.ExportedpatternNorepeat
			Integer[] order=new Integer[learnitem.ExportedpatternNorepeat.size()];
			String path=learnitem.allpath_From_Max_To_s0.get(0);
			int i;
			for(i=0;i<=learnitem.ExportedpatternNorepeat.size()-1;i++){
				String r=learnitem.ExportedpatternNorepeat.get(i);
				int j=path.indexOf(r);
				order[i]=j;
			}
			
			////sort order

	      	boolean swapped = true;
	      	int p = 0;
	      	Integer  tmpI;
	      	String tmpS;
	      	while (swapped){
	      		swapped = false;
	              p++;
	              for (i = 0; i < order.length - p; i++) {
	              		if (order[i] > order[i+1]) {
	                            tmpI=order[i];
	                            order[i+1]=order[i];
	                            order[i]=tmpI;
	              			
	              				tmpS = learnitem.ExportedpatternNorepeat.get(i);
	              				learnitem.ExportedpatternNorepeat.set(i, learnitem.ExportedpatternNorepeat.get(i+1));
	              				learnitem.ExportedpatternNorepeat.set(i+1,tmpS);
	                            swapped = true;
	                      }
	                }
	          }
			
			
				
			 ALearningItems.set(x,learnitem);
		}
		
		//////Sort the learnitems according to the length of learnitem.ExportedpatternNorepeat descendingly 
		
		for (int i = 0; i < ALearningItems.size() - 1; i++)
	    {
	        int index = i;
	        for (int j = i + 1; j < ALearningItems.size(); j++)
	            if (ALearningItems.get(j).ExportedpatternNorepeat.size() > ALearningItems.get(i).ExportedpatternNorepeat.size()) 
	                index = j;
	 
	        LearningItem temp = ALearningItems.get(index);  
	        ALearningItems.set(index,ALearningItems.get(i));
	        ALearningItems.set(i,temp);
	    }
	 
			
	}
	private void FindFreqPatt_ECLAT(LearningItem learnitem,double minsup){
		ArrayList<String>  allpath=learnitem.allpath_From_Max_To_s0;
		Map<String, Integer> mapItemsToInt = new HashMap<String, Integer>();
		int n = 0;
		for (int m=0; m<=allpath.size()-1; m++){
			String[] path=allpath.get(m).split(",");
			for(String rule : path) {
				if (!mapItemsToInt.keySet().contains(rule))
					mapItemsToInt.put(rule, n++);				
			}
		}
		TransactionDatabase database = new TransactionDatabase(allpath, mapItemsToInt);
		AlgoEclat eclat = new AlgoEclat(database, mapItemsToInt);
		learnitem.Exportedpattern = eclat.runAlgorithm(minsup);
					
			
	}
public void dfs_pathCycle(GraphState prestate,Rule rule,GraphState state,Set<? extends GraphState> nodeset,Set<? extends GraphTransition> edgeset,ExploringAuto exploringAuto){
		
	     int x=0;
		if(rule!=null && rule.toString().equals("reach_2"))
			x++;
	
		//// prestate-----rule--->state///////////////
		GrammarModel grammermodel=simulator.getModel().getGrammar();
		ExploringAuto.PartialPath prepartialPath=null;
		/////Detect a cycle
		if(rule!=null){
			for(int i=0;i<=exploringAuto.allPartialPaths.size()-1;i++){
				prepartialPath=exploringAuto.allPartialPaths.get(i);
				if(prepartialPath.curStateRule.state.equals(prestate)){
					for(int j=prepartialPath.pathS0_to_CurState.size()-1;j>=0;j--){
						ExploringAuto.StateRule staterule=prepartialPath.pathS0_to_CurState.get(j);
						if(staterule.state.equals(state)){
							ExploringAuto.StateRule newstaterule=exploringAuto.getNewStateRule();
							newstaterule.state=state;
							newstaterule.rule=rule;
							prepartialPath.pathS0_to_CurState.add(newstaterule);
							exploringAuto.pathLeadCycle=prepartialPath.pathS0_to_CurState;
							exploringAuto.startIndexofCycle=j+1;
							exploringAuto.isFindpathLeadCycle=true;
							return;
						}
					}
				}
			}
		}else{      ///rule==null && state=s0 
			prepartialPath=exploringAuto.getNewPartialPath();
			ExploringAuto.StateRule newstaterule=exploringAuto.getNewStateRule();
			newstaterule.state=state;
			newstaterule.rule=rule;
			prepartialPath.curStateRule=newstaterule;
			prepartialPath.pathS0_to_CurState.add(newstaterule);  
			exploringAuto.allPartialPaths.add(prepartialPath);
		}
		//////////////////
		
		if(exploringAuto.isFindpathLeadCycle==true)
			return;
	
		/////Is this state satisfy the ModelCheckingTarget property?
		boolean isfind=false;
		Set<? extends GraphTransition> grtr=state.getTransitions();
		for(GraphTransition gt:grtr){
			GraphState sourceState=gt.source();
			if(gt.isLoop() && sourceState.equals(state)){
				if(gt.text(false).equals(ModelCheckingTarget)){
					isfind=true;
					break;
				}
			}
		}
		
		if(exploringAuto.isFindpathLeadCycle==true)
			return;
		
		if(isfind==true)
			return;
		else if(rule!=null){//////////add this state to pre partial paths
			for(int i=0;i<=exploringAuto.allPartialPaths.size()-1;i++){
				prepartialPath=exploringAuto.allPartialPaths.get(i);
				if(prepartialPath.curStateRule.state.equals(prestate)){
					ExploringAuto.PartialPath newpartialPath=exploringAuto.getNewPartialPath();
					ExploringAuto.StateRule newstaterule=exploringAuto.getNewStateRule();
					newstaterule.state=state;
					newstaterule.rule=rule;
					newpartialPath.curStateRule=newstaterule;
					newpartialPath.pathS0_to_CurState=(ArrayList<ExploringAuto.StateRule>)prepartialPath.pathS0_to_CurState.clone();
					newpartialPath.pathS0_to_CurState.add(newstaterule);
					exploringAuto.allPartialPaths.add(newpartialPath);
				}
			}
		}
		
		if(exploringAuto.isFindpathLeadCycle==true)
			return;
		
		/////////
		////is all states checked?
		if(exploringAuto.checkedStates.size()==nodeset.size())
			return;
		
		/////////////
		/////check for next states of this state
		try{
			grtr=state.getTransitions();
			for(GraphTransition gt:grtr){
				GraphState sourceState=gt.source();
				GraphState targetState=gt.target();
				if(!gt.isLoop() && sourceState.equals(state)){
					GraphState nextState=targetState;
					RuleModel rulemodel=grammermodel.getRuleModel(QualName.name(gt.text(false)));
				    Rule nextrule=rulemodel.toResource().getCondition().getRule();
				    exploringAuto.checkedStates.add(nextState);
				    dfs_pathCycle(state, nextrule, nextState, nodeset, edgeset,exploringAuto);
				    if(exploringAuto.isFindpathLeadCycle==true)
						return;
				}
			}
		}
		catch (FormatException e) {
	           System.err.println(e.getMessage());
	    }
		////////////////////
	}
	
	private void FindFreqPatt_Apriori(LearningItem learnitem,double minsup){
		ArrayList<String>  allpath=learnitem.allpath_From_Max_To_s0;
			
		////make C_1
		
		String[] s=allpath.get(0).split(",");
		if(learnitem.C1_Items.size()==0){
			for(int i=0;i<=s.length-1;i++){
				boolean flag=false;
				for(int j=0;j<=learnitem.C1_Items.size()-1;j++){
					if(learnitem.C1_Items.get(j).rules.equals(s[i]))
					{flag=true;break;}
				}
				if(!flag){
					LearningItem.Item item=learnitem.getNewItem();
					item.rules=s[i];
					learnitem.C1_Items.add(item);
				}
			}
			for(int i=0;i<=learnitem.C1_Items.size()-1;i++){
				String p=learnitem.C1_Items.get(i).rules.toString();
				learnitem.C1_Items.get(i).support=findsupport2(allpath, p);
			}
		}
		
		//////////////////////
		learnitem.CK_Items=(ArrayList<Item>)learnitem.C1_Items.clone();
		
			
		for(int k=1;learnitem.CK_Items.size()>0 && learnitem.CK_Items.get(0).rules.split(",").length<=learnitem.allRules.size();k++){
			//////////////make C_k+1
			learnitem.Ctemp_Items.clear();
			for(int i=0;i<=learnitem.CK_Items.size()-1;i++)
				for(int j=0;j<=learnitem.C1_Items.size()-1;j++){
					if(!learnitem.CK_Items.get(i).rules.contains(learnitem.C1_Items.get(j).rules)){
						LearningItem.Item item=learnitem.getNewItem();
						item.rules=learnitem.CK_Items.get(i).rules+","+learnitem.C1_Items.get(j).rules;
						learnitem.Ctemp_Items.add(item);
					}
				}
			for(int i=0;i<=learnitem.Ctemp_Items.size()-1;i++){
				String p=learnitem.Ctemp_Items.get(i).rules.toString();
				learnitem.Ctemp_Items.get(i).support=findsupport2(allpath, p);
			}
			
			
			learnitem.Cresp_Items=(ArrayList<Item>)learnitem.CK_Items.clone();
			//remove  items that their support less than minsup 
			learnitem.CK_Items.clear();
			for(int r=0;r<=learnitem.Ctemp_Items.size()-1;r++){
				if(learnitem.Ctemp_Items.get(r).support>=minsup){
					learnitem.CK_Items.add(learnitem.Ctemp_Items.get(r));
				}
			}
		}///end of for
		///////////////
		
		learnitem.Exportedpattern="";
		int max=0;
		for(int i=1;i<=learnitem.Cresp_Items.size()-1;i++){
			if(learnitem.Cresp_Items.get(i).support>learnitem.Cresp_Items.get(max).support)
				max=i;
		}
		if(max<=learnitem.Cresp_Items.size()-1)
			learnitem.Exportedpattern=learnitem.Cresp_Items.get(max).rules;
				
			
	}
	
	/*
	private String reverse(String s){
		String t="";
		for(int j=0;j<=s.length()-1;j++){
			t=t.concat(s.substring(s.length()-1-j,1));
		}
		return t;
	}*/
	
	
	private double findsupport(ArrayList<String>  allpath,String p){
		int freq=0;
		int allfreq=allpath.size();
		for(int i=0;i<=allpath.size()-1;i++){
			String s=allpath.get(i);
			if(s.contains(p))
				freq++;
			
		}
		return (double)freq/allfreq;
		
	}
	/*
	private double findsupport3(ArrayList<String>  allpath,String p){
		int freq=0;
		String[] ap=p.split(",");
		for(int i=0;i<=allpath.size()-1;i++){
			String s=allpath.get(i);
			boolean flag=true;
			int j=0;
			int k=0;
			while(flag && j<=ap.length-1){
				k=s.indexOf(ap[j]);
				if(k<0)
					flag=false;
				if(k>0)
					s=s.substring(0,k-1)+s.substring(k+ap[j].length());
				else if (k==0)
					s=s.substring(k+ap[j].length());
				j++;
			}
			if(flag)
				freq++;
		}
		return (double)freq/allpath.size();
	}
	*/
	private double findsupport2(ArrayList<String>  allpath,String p){
		int freq=0;
		String[] ap=p.split(",");
		for(int i=0;i<=allpath.size()-1;i++){
			String s=allpath.get(i);
			boolean flag=true;
			int j=0;
			int k=0;
			while(flag && j<=ap.length-1){
				k=s.indexOf(ap[j],k);
				if(k<0)
					flag=false;
				k+=ap[j].length()+1;
				j++;
			}
			if(flag)
				freq++;
		}
		return (double)freq/allpath.size();
	}

	
	private void dfs(GraphState curState,String path ,LearningItem learnitem,GrammarModel grammermodel,Set<? extends GraphState> nodeset){
		
		if(learnitem.allpath_From_Max_To_s0.size()>=100)
			return;
		
		if(curState.toString().equals("s0")){
			learnitem.allpath_From_Max_To_s0.add(path);
		}
		else{
			ArrayList<String> prevStates=find_ALL_prevStates(grammermodel, nodeset, curState);
			for(int i=0;i<=prevStates.size()-1;i+=2){
				String prevstateS=prevStates.get(i);
				GraphState prevstate=null;
				String rulename=prevStates.get(i+1);
				for(GraphState ns :nodeset){
					if(ns.toString().equals(prevstateS)){
						prevstate=ns;
						break;
					}
						
				}
				
				if(learnitem.allpath_From_Max_To_s0.size()>=100)
					return;
				
				dfs(prevstate,rulename+","+path, learnitem, grammermodel, nodeset);
			}
		}
		
	}
	
	private  ArrayList<String>  find_ALL_prevStates(GrammarModel grammermodel,Set<? extends GraphState> nodeset,GraphState curState){
		////s0,go_hungry,s1,get_left,....
		ArrayList<String> prevStates=new ArrayList<String>();
		
		int curdepth=0,prevdepth=0;
		
		GraphState prevState=null;
		Rule r=null;
		try{
			for(GraphState ns :nodeset){
				Set<? extends GraphTransition> grtr=ns.getTransitions();
				for(GraphTransition gt:grtr){
					GraphState sourceState=gt.source();
					GraphState targetState=gt.target();
					if(targetState.equals(curState)){
						prevState=sourceState;
						RuleModel rulemodel=grammermodel.getRuleModel(QualName.name(gt.text(false)));
					    r=rulemodel.toResource().getCondition().getRule();
					    if(prevState.getNumber()<curState.getNumber() && !prevStates.contains(prevState.toString()) ){
					    	prevStates.add(prevState.toString());
					    	prevStates.add(r.getQualName().toString());
					    }
					}
						
				}
			}
		}
		catch (FormatException e) {
	           System.err.println(e.getMessage());
	    }
		
		return prevStates;
	}

	
	
	private  Collection<GraphState>  FindTargetStates(Set<? extends GraphTransition> edgest ){
		Collection<GraphState> TargetStates=new ArrayList<GraphState>();
		for(GraphTransition gt:edgest){
			if(gt.source().equals(gt.target()))
				if(gt.text(false).equals(ModelCheckingTarget)){
					GraphState gs=gt.source();
					TargetStates.add(gs);
				}
				
		}
		
		
		
		return TargetStates;
	}
	
	 private ArrayList<String> removeRepeatRules(ArrayList<String> allRulesNames){
	    	ArrayList<String> arules=new ArrayList<String>();
	    	
	    	for(int i=0;i<=allRulesNames.size()-1;i++){
	    		String s=allRulesNames.get(i);
	    		if (!arules.contains(s))
	    			arules.add(s);
	    	}
	    	return arules;
	}
	
	private GraphState findPrevState(GrammarModel grammermodel,Set<? extends GraphState> nodeset,GraphState curState){
		GraphState prevState=null;
		Rule r=null;
		try{
			for(GraphState ns :nodeset){
				Set<? extends GraphTransition> grtr=ns.getTransitions();
				boolean isfind=false;
				for(GraphTransition gt:grtr){
					GraphState sourceState=gt.source();
					GraphState targetState=gt.target();
					if(targetState.equals(curState)){
						prevState=sourceState;
						RuleModel rulemodel=grammermodel.getRuleModel(QualName.name(gt.text(false)));
					    r=rulemodel.toResource().getCondition().getRule();
					    isfind=true;
						break;
					}
						
				}
				if (isfind)
					break;
			}
		}
		catch (FormatException e) {
	           System.err.println(e.getMessage());
	    }
		ruleHelp=r;
		return prevState;
	}
	private Rule ruleHelp;
	

	public String start(){
		//try
		//{
			
			simulator.getModel().resetGTS();  //Creates a fresh GTS and fires an update event.
			GTS gts = simulator.getModel().getGTS();
			final SimulatorModel simulatorModel = simulator.getModel();
			ExploreType exploreType=simulatorModel.getExploreType();	
			GraphState state = simulatorModel.getState();
			
			
			
			
		  
			String heuristicResult=null;
			
			 ALearningItems.get(0).simulator=simulator;
	    
			 ALearningItems.get(0).isProgressVisible=false;
			 
			 ALearningItems.get(0).Number_Explored_States=0;
			 
			 HeuStyleExploreAction heuExploreAction=new HeuStyleExploreAction(simulator, false);
			 boolean  isFirstStep=true;
			 heuExploreAction.explore(exploreType, ALearningItems,ModelCheckingType,ModelCheckingTarget,isFirstStep);
					 
			 heuristicResult=ALearningItems.get(0).heuristicResult;
	     	
			 ALearningItems.get(0).isProgressVisible=false;
			 
			 if(heuristicResult==null){
				 int Maxrepeat=1000;
				 int repeat=1;
				 while(repeat<=Maxrepeat && ALearningItems.get(0).heuristicResult==null){
					
					 simulator.getModel().resetGTS();
					 simulator.getModel().getGTS().nodeSet().clear();
	   		         gts=simulator.getModel().getGTS();
	
				     exploreType=simulatorModel.getExploreType();
					 heuExploreAction=new HeuStyleExploreAction(simulator, false);
					 isFirstStep=false;
					 heuExploreAction.explore(exploreType, ALearningItems,ModelCheckingType,ModelCheckingTarget,isFirstStep);
					 repeat++;
				 }
			 }
			 
			 //if(!ALearningItems.get(0).isProgressVisible)
				// simulator.getModel().resetGTS();
			 
			if(ALearningItems.get(0).heuristicResult==null)
				simulator.getModel().resetGTS();
			 
			 heuristicResult=ALearningItems.get(0).heuristicResult;
			 gts=simulator.getModel().getGTS();
			 
			 
			 if(ALearningItems.get(0).heuristicResult.equals("reachability")){
					LTSDisplay ltsDisplay=(LTSDisplay)ALearningItems.get(0).simulator.getDisplaysPanel().getDisplay(DisplayKind.LTS);
					ArrayList<GraphState> result=new ArrayList<GraphState>();
					
					result.add(ALearningItems.get(0).simulator.getModel().getGTS().startState());
					for(int i=0;i<=ALearningItems.get(0).pathLeadCycleInLargeModel.size()-1;i++)
						result.add(ALearningItems.get(0).pathLeadCycleInLargeModel.get(i).state);
					ltsDisplay.emphasiseStates(result, true);
			 }
			 
			 
			 Number_Explored_States=ALearningItems.get(0).Number_Explored_States;
			 
		     return heuristicResult;
	     
		//}
		//catch (FormatException e) {
        //  System.err.println(e.getMessage());
		  //}
	//	return null;
	     
	     
	     
	}
    
 	
 	
	
	
	public Simulator simulator;
	public String HostGraphName;
	public String ModelCheckingType;
	public String ModelCheckingTarget;
	public String DataMiningType;
	
	private GraphState initialState;
	private String initialStateName;
	
	/**
	 * Save all paths from final state's to initial state.
	*/
	public ArrayList<LearningItem> ALearningItems;
	public long Number_Explored_States;
	
	 
}

		