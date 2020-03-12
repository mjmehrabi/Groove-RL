package groove.verify;

import groove.grammar.Condition;
import groove.grammar.Grammar;
import groove.grammar.QualName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import groove.explore.Exploration;
import groove.explore.ExploreType;
import groove.grammar.aspect.AspectEdge;
//import groove.grammar.model.FormatException;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.model.RuleModel;
import groove.grammar.rule.RuleEdge;
import groove.grammar.rule.RuleNode;
import groove.gui.Simulator;
import groove.gui.SimulatorModel;
import groove.gui.action.HeuBOAExploreAction;

import groove.gui.display.DisplayKind;
import groove.gui.display.LTSDisplay;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.util.parse.FormatException;
import groove.verify.BaysianNetwork.Nodeitem;
import groove.verify.ExploringGaBayesNet.Chromosome;

/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */


public class HeuBOA{
	public HeuBOA(){
		exploreGaBayesNet=new ExploringGaBayesNet();
	}
	
	public void EnableSelectedHostGraph(){
        try {
            simulator.getModel().doEnableUniquely(ResourceKind.HOST, QualName.name(HostGraphName));
        } catch (IOException exc) {
        	System.err.println("Error during %s enabling" +ResourceKind.HOST.getDescription());
        }
        
	}
	
	public String start(String targetRule,String BOAType,String SelectionType,int RulesCount,ArrayList<QualName> RulesName,Grammar grammer,GrammarModel grammermodel){
		
		
		//BoAmAiN boaMain=new BoAmAiN();
		//boaMain.executeBOA();
		//return null;
		
	
		
		if(!callFromHeuGenerator){
			grammermodel=simulator.getModel().getGrammar();
			
		}
		
		
		
		//////////////////////////////////////////////////////
		if(callFromHeuGenerator){
			
			SelectionRate=CrossOverRate;
			ReplacementRate=MutationRate;
			
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
		        				// do nothings
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
		
		
			if(exploreGaBayesNet==null)
				exploreGaBayesNet=new ExploringGaBayesNet();
			exploreGaBayesNet.callFromHeuGenerator=callFromHeuGenerator;
			exploreGaBayesNet.HostGraphName=HostGraphName;
			exploreGaBayesNet.grammer=grammer;
			exploreGaBayesNet.grammermodel=grammermodel;
			ExploreType exploreType=null;
			if(!callFromHeuGenerator){
				simulator.getModel().resetGTS();  //Creates a fresh GTS and fires an update event.
				GTS gts = simulator.getModel().getGTS();
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
				exploreGaBayesNet.gts=gts;
				exploreGaBayesNet.initialState=gts.startState();
				exploreGaBayesNet.exploreType=exploreType;
			}
						
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
                			exploreGaBayesNet.targetGraph_edgeList.add(re);
                			if(!exploreGaBayesNet.targetGraph_nodeList.contains(re.source()))
                				exploreGaBayesNet.targetGraph_nodeList.add(re.source());
                			if(!exploreGaBayesNet.targetGraph_nodeList.contains(re.target()))
                				exploreGaBayesNet.targetGraph_nodeList.add(re.target());
                		}
                		for(Condition cond : allcond){
                			ExploringGaBayesNet.NAC nac=exploreGaBayesNet.getNewNAC();
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
                			exploreGaBayesNet.allNACs.add(nac);
                		}
                		break;
            		}
           	}
						
			//////////////////////////////////////////
			//////////////////////////////////////////

			exploreGaBayesNet.RulesCount=RulesCount;
			exploreGaBayesNet.RulesName=RulesName;
			
			
			exploreGaBayesNet.timeLimit=timeLimit;
			exploreGaBayesNet.isContinue=isContinue;
			exploreGaBayesNet.lastTime=lastTime;
			
			exploreGaBayesNet.CountOFpopulation=CountOFpopulation;
			exploreGaBayesNet.Iterations=Iterations;
			exploreGaBayesNet.DepthOfSearch=DepthOfSearch+1;
			exploreGaBayesNet.MutationRate=MutationRate;
			exploreGaBayesNet.CrossOverRate=CrossOverRate;
			exploreGaBayesNet.BOAType=BOAType;
			exploreGaBayesNet.SelectionType=SelectionType;
			this.SelectionType=SelectionType;
			
			exploreGaBayesNet.targetRule=targetRule;
			exploreGaBayesNet.ModelCheckingTarget=ModelCheckingTarget;
			exploreGaBayesNet.simulator=simulator;
			
			
			exploreGaBayesNet.Number_Explored_States=0;
			exploreGaBayesNet.First_Found_Dead_depth=0;
			exploreGaBayesNet.First_Found_Dead_Rep=0;
			exploreGaBayesNet.Call_Number_Fitness=0;
			exploreGaBayesNet.RunningTime_AllFitnessFuncs=0;
			
						
	    	
			////////////////////////////////////
			ArrayList<QualName> Alltype=new ArrayList<QualName>();
    		
        	//grammermodel=exploreGaBayesNet.simulator.getModel().getGrammar();
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

           	exploreGaBayesNet.Alltype=Alltype;
           	
			////////////////////////////
           	exploreGaBayesNet.chroCountForLearnBayesNet=(int)((exploreGaBayesNet.CountOFpopulation*SelectionRate));  // 40%
           	exploreGaBayesNet.chroCountReplaceBySampling=(int)((exploreGaBayesNet.CountOFpopulation*ReplacementRate));  // 50%
           	//////////////////////////////////////////////////
           	
           			
			
			exploreGaBayesNet.isProgressVisible=false;
			if(callFromHeuGenerator)
				exploreGaBayesNet.isProgressVisible=false;
			exploreGaBayesNet.WhatStep="CIP";  //createInitialPopulation
			
			
			

			///////////BOA//////////////////////BOA/////////////////BOA///////////////////////////////////
			///////////BOA//////////////////////BOA/////////////////BOA///////////////////////////////////
			///////////BOA//////////////////////BOA/////////////////BOA///////////////////////////////////
			long ppStateCount=-1;
			long pStateCount=-1;
			long curStateCount=0;
			if(exploreGaBayesNet.BOAType.equals("naiveBOA")|| exploreGaBayesNet.BOAType.equals("chainBOA") || exploreGaBayesNet.BOAType.equals("tpBOA") || exploreGaBayesNet.BOAType.equals("3pBOA") || exploreGaBayesNet.BOAType.equals("4pBOA") ){ //Genetic & Learning of Baysian Networks Algoritms
				createInitialPopulation_BOA(exploreGaBayesNet,exploreType);
				if(exploreGaBayesNet.heuristicResult!=null && exploreGaBayesNet.isContinue){
	    			if(Number_Explored_States==0){  //first goal state found
	    				Number_Explored_States=exploreGaBayesNet.Number_Explored_States;
	    				First_Found_Dead_depth=exploreGaBayesNet.First_Found_Dead_depth;
	    				First_Found_Dead_Rep=exploreGaBayesNet.First_Found_Dead_Rep;
	    				Call_Number_Fitness=exploreGaBayesNet.Call_Number_Fitness;
	    				RunningTime_AllFitnessFuncs=exploreGaBayesNet.RunningTime_AllFitnessFuncs;
	    			}  
    					    			    				
    				while(System.currentTimeMillis()<exploreGaBayesNet.lastTime && exploreGaBayesNet.heuristicResult!=null){
    					ExploringGaBayesNet.GoalState goalstate=exploreGaBayesNet.getNewGoalState();
        				goalstate.foundTime=System.currentTimeMillis();
        				goalstate.witnessLength=exploreGaBayesNet.First_Found_Dead_depth;
        				exploreGaBayesNet.goalStatesInfo.add(goalstate);
        				if(System.currentTimeMillis()<exploreGaBayesNet.lastTime){
    	    				exploreGaBayesNet.heuristicResult=null;
    	    				exploreGaBayesNet.Number_Explored_States=0;
    	    				exploreGaBayesNet.First_Found_Dead_depth=0;
    	    				exploreGaBayesNet.Call_Number_Fitness=0;
    	    				exploreGaBayesNet.RunningTime_AllFitnessFuncs=0;
    	    				
    	    			}
        				createInitialPopulation_BOA(exploreGaBayesNet,exploreType);
    				}
    				if(System.currentTimeMillis()>=exploreGaBayesNet.lastTime && exploreGaBayesNet.goalStatesInfo.size()>0)
    					exploreGaBayesNet.heuristicResult="rechability";
    				
	    		}

				
				if(exploreGaBayesNet.heuristicResult==null) 
					sortPopulation(exploreGaBayesNet);
				if(!exploreGaBayesNet.ModelCheckingTarget.equals("DeadLock") && exploreGaBayesNet.heuristicResult==null ){ ////Reachability
	    			///////Reachability
					for (int iter = 0; iter <= exploreGaBayesNet.Iterations-1 && exploreGaBayesNet.heuristicResult==null ; iter++){
						exploreGaBayesNet.First_Found_Dead_Rep++;
						if(exploreGaBayesNet.BOAType.equals("naiveBOA")){
							ppStateCount=pStateCount;
							pStateCount=curStateCount;
							if(!exploreGaBayesNet.callFromHeuGenerator)
								curStateCount=exploreGaBayesNet.simulator.getModel().getGTS().nodeCount();
							else
								curStateCount=exploreGaBayesNet.gts.nodeCount();
							
	    					if(Math.abs(pStateCount-curStateCount)<=2 && Math.abs(ppStateCount-pStateCount)<=2){
	    						///the learned information by naiveBOA is incorrect
	    						exploreGaBayesNet.WhatStep="CIP";  //createInitialPopulation
	    						createInitialPopulation_BOA(exploreGaBayesNet,exploreType);
	    						ppStateCount=-1;
	    						pStateCount=-1;
		    					curStateCount=0;
	    						if(exploreGaBayesNet.heuristicResult==null) 
	    							sortPopulation(exploreGaBayesNet);
	    					}
	    					LearningOfNaiveBaysianNetwork_naiveBOA(exploreGaBayesNet);
						}
	    				else if (exploreGaBayesNet.BOAType.equals("chainBOA"))
	    					LearningOfChainBaysianNetwork_chainBOA(exploreGaBayesNet);
	    				else if (exploreGaBayesNet.BOAType.equals("tpBOA"))
	    					LearningOfBayesianNetwork_tpBOA(exploreGaBayesNet);
	    				else if (exploreGaBayesNet.BOAType.equals("3pBOA"))
	    					LearningOfBayesianNetwork_3pBOA(exploreGaBayesNet);
	    				else
	    					LearningOfBayesianNetwork_4pBOA(exploreGaBayesNet);
						
						
						
						exploreGaBayesNet.WhatStep="SACFN";  //Sampling_and_CalcFitness
			    		Sampling_and_CalcFitness(exploreGaBayesNet);
			    		if(exploreGaBayesNet.heuristicResult==null) 
			    			sortPopulation(exploreGaBayesNet);
			    		
			    		
			    		if(exploreGaBayesNet.heuristicResult!=null && exploreGaBayesNet.isContinue){
			    			if(Number_Explored_States==0){  //first goal state found
			    				Number_Explored_States=exploreGaBayesNet.Number_Explored_States;
			    				First_Found_Dead_depth=exploreGaBayesNet.First_Found_Dead_depth;
			    				First_Found_Dead_Rep=exploreGaBayesNet.First_Found_Dead_Rep;
			    				Call_Number_Fitness=exploreGaBayesNet.Call_Number_Fitness;
			    				RunningTime_AllFitnessFuncs=exploreGaBayesNet.RunningTime_AllFitnessFuncs;
			    			}  
		    				
			    			ExploringGaBayesNet.GoalState goalstate=exploreGaBayesNet.getNewGoalState();
		    				goalstate.foundTime=System.currentTimeMillis();
		    				goalstate.witnessLength=exploreGaBayesNet.First_Found_Dead_depth;
		    				exploreGaBayesNet.goalStatesInfo.add(goalstate);
			    			
		    				if(System.currentTimeMillis()<exploreGaBayesNet.lastTime){
			    				exploreGaBayesNet.heuristicResult=null;
			    				exploreGaBayesNet.Number_Explored_States=0;
			    				exploreGaBayesNet.First_Found_Dead_depth=0;
			    				exploreGaBayesNet.Call_Number_Fitness=0;
			    				exploreGaBayesNet.RunningTime_AllFitnessFuncs=0;
			    				
			    			}
			    		}

			    		
			    	}
				}else{
					/////////Deadlock
	    			for (int iter = 0; iter <= exploreGaBayesNet.Iterations-1 && exploreGaBayesNet.heuristicResult==null ; iter++){
	    				exploreGaBayesNet.First_Found_Dead_Rep++;
	    				if(exploreGaBayesNet.BOAType.equals("naiveBOA")){
	    					ppStateCount=pStateCount;
	    					pStateCount=curStateCount;
	    					if(!exploreGaBayesNet.callFromHeuGenerator)
								curStateCount=exploreGaBayesNet.simulator.getModel().getGTS().nodeCount();
							else
								curStateCount=exploreGaBayesNet.gts.nodeCount();
	    					
	    					if(Math.abs(pStateCount-curStateCount)<=2 && Math.abs(ppStateCount-pStateCount)<=2){
	    						///the learned information by naiveBOA is incorrect
	    						exploreGaBayesNet.WhatStep="CIP";  //createInitialPopulation
	    						createInitialPopulation_BOA(exploreGaBayesNet,exploreType);
	    						
	    						ppStateCount=-1;
	    						pStateCount=-1;
		    					curStateCount=0;
	    						if(exploreGaBayesNet.heuristicResult==null) 
	    							sortPopulation(exploreGaBayesNet);
	    					}
	    					LearningOfNaiveBaysianNetwork_naiveBOA(exploreGaBayesNet);
	    				}
	    				else if (exploreGaBayesNet.BOAType.equals("chainBOA"))
	    					LearningOfChainBaysianNetwork_chainBOA(exploreGaBayesNet);
	    				else if (exploreGaBayesNet.BOAType.equals("tpBOA"))
	    					LearningOfBayesianNetwork_tpBOA(exploreGaBayesNet);
	    				else if (exploreGaBayesNet.BOAType.equals("3pBOA"))
	    					LearningOfBayesianNetwork_3pBOA(exploreGaBayesNet);
	    				else
	    					LearningOfBayesianNetwork_4pBOA(exploreGaBayesNet);
	    				
	    				
	    				exploreGaBayesNet.WhatStep="SACFN";  //Sampling_and_CalcFitness
	    				Sampling_and_CalcFitness(exploreGaBayesNet);
	    				if(exploreGaBayesNet.heuristicResult==null) 
			    			sortPopulation(exploreGaBayesNet);
	    				
			    		if(exploreGaBayesNet.heuristicResult!=null && exploreGaBayesNet.isContinue){
			    			if(Number_Explored_States==0){  //first goal state found
			    				Number_Explored_States=exploreGaBayesNet.Number_Explored_States;
			    				First_Found_Dead_depth=exploreGaBayesNet.First_Found_Dead_depth;
			    				First_Found_Dead_Rep=exploreGaBayesNet.First_Found_Dead_Rep;
			    				Call_Number_Fitness=exploreGaBayesNet.Call_Number_Fitness;
			    				RunningTime_AllFitnessFuncs=exploreGaBayesNet.RunningTime_AllFitnessFuncs;
			    			}  
		    				
			    			ExploringGaBayesNet.GoalState goalstate=exploreGaBayesNet.getNewGoalState();
		    				goalstate.foundTime=System.currentTimeMillis();
		    				goalstate.witnessLength=exploreGaBayesNet.First_Found_Dead_depth;
		    				exploreGaBayesNet.goalStatesInfo.add(goalstate);
			    			
		    				if(System.currentTimeMillis()<exploreGaBayesNet.lastTime){
			    				exploreGaBayesNet.heuristicResult=null;
			    				exploreGaBayesNet.Number_Explored_States=0;
			    				exploreGaBayesNet.First_Found_Dead_depth=0;
			    				exploreGaBayesNet.Call_Number_Fitness=0;
			    				exploreGaBayesNet.RunningTime_AllFitnessFuncs=0;
			    				
			    			}
			    		}

			    	}
				}
			}
		
			
			
			if(!callFromHeuGenerator==true)
				if(exploreGaBayesNet.heuristicResult==null)
					simulator.getModel().resetGTS();
			
			if(exploreGaBayesNet.heuristicResult==null)
				exploreGaBayesNet.heuristicResult="noreachability";
       	    
			////////////////////////////////
			///////////////////////////////
			if(callFromHeuGenerator==true){
				if(exploreGaBayesNet.heuristicResult.equals("reachability")){
					ExploringGaBayesNet.Chromosome chromosome=exploreGaBayesNet.population.get(exploreGaBayesNet.chroIndexCounterExamlpe);
					return "The property is verified."+" Target state found in depth:"+ exploreGaBayesNet.First_Found_Dead_depth +" The number of explored states:"+exploreGaBayesNet.Number_Explored_States +" The number of fitness calls:"+exploreGaBayesNet.Call_Number_Fitness+" The running time of all fitness funcs:"+exploreGaBayesNet.RunningTime_AllFitnessFuncs+" ";
				}
				else
					return "The property is not verified.";
			}
			////////////////////////////////
			///////////////////////////////
			
			if(exploreGaBayesNet.goalStatesInfo.size()>0)
				exploreGaBayesNet.heuristicResult="reachability";
			
			
			if(exploreGaBayesNet.heuristicResult.equals("reachability")){
				LTSDisplay ltsDisplay=(LTSDisplay)exploreGaBayesNet.simulator.getDisplaysPanel().getDisplay(DisplayKind.LTS);
				ArrayList<GraphState> result=new ArrayList<GraphState>();
				
				result.add(exploreGaBayesNet.simulator.getModel().getGTS().startState());
				ExploringGaBayesNet.Chromosome chromosome=exploreGaBayesNet.population.get(exploreGaBayesNet.chroIndexCounterExamlpe);
				for(int i=0;i<=chromosome.states.size()-1;i++)
					result.add(chromosome.states.get(i));
				//result.add(exploreGaBayesNet.lastStateInReachability);
				ltsDisplay.emphasiseStates(result, true);
			}
			
			if(!exploreGaBayesNet.isContinue){
				Number_Explored_States=exploreGaBayesNet.Number_Explored_States;
				First_Found_Dead_depth=exploreGaBayesNet.First_Found_Dead_depth;
				First_Found_Dead_Rep=exploreGaBayesNet.First_Found_Dead_Rep;
				Call_Number_Fitness=exploreGaBayesNet.Call_Number_Fitness;
				RunningTime_AllFitnessFuncs=exploreGaBayesNet.RunningTime_AllFitnessFuncs;
			}			
			
			if(exploreGaBayesNet.heuristicResult.equals("reachability"))
				return exploreGaBayesNet.heuristicResult+"_"+exploreGaBayesNet.lastStateInReachability.toString();
			else
				return exploreGaBayesNet.heuristicResult;
	    
	}
	
	
	private void Sampling_and_CalcFitness(ExploringGaBayesNet exploreGaBayesNet){
		///exploreGaBayesNet.chroCountReplaceBySampling for replace with sampling
		exploreGaBayesNet.totalFitness=0;
		ExploreType exploreType = null;
		if(!callFromHeuGenerator){
    		simulator.getModel().resetGTS();
    		exploreType=exploreGaBayesNet.simulator.getModel().getExploreType();
    		exploreGaBayesNet.exploreType=exploreType;
    	}else{
    		GTS gts=null;
			try {
				gts = new GTS(exploreGaBayesNet.grammer);
			} catch (FormatException e) {
				// do nothing
				e.printStackTrace();
			}
			exploreGaBayesNet.gts=gts;
			exploreGaBayesNet.initialState=gts.startState();
    	}
		HeuBOAExploreAction heuExploreAction=new HeuBOAExploreAction(simulator, false);
		heuExploreAction.explore(exploreType, exploreGaBayesNet);
	   
	}

	
   
   private void LearningOfNaiveBaysianNetwork_naiveBOA(ExploringGaBayesNet exploreGaBayesNet){
    	
    	/////exploreGaBayesNet.chroCountForLearnBayesNet For Learning of Naive Bayes Network
    	////Rule_current--->Rule_next 
	
		exploreGaBayesNet.baysNet.Nodes.clear();
		
		
		///add the first node 
    	BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.getNewNode();
    	for(int i=0;i<=exploreGaBayesNet.RulesCount-1;i++){
    		BaysianNetwork.Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
    		nodeitem.curRulename=exploreGaBayesNet.RulesName.get(i).toString();
    		nodeitem.prevRulename="";
    		curnode.NodeItems.add(nodeitem);
    	}
    	exploreGaBayesNet.baysNet.Nodes.add(curnode);
    	
    	//add the next nodes
    	BaysianNetwork.Node nextnode=exploreGaBayesNet.baysNet.getNewNode();
    	for(int i=0;i<=exploreGaBayesNet.RulesCount-1;i++)
    		for(int j=0;j<=exploreGaBayesNet.RulesCount-1;j++){
    			BaysianNetwork.Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
    			nodeitem.curRulename=exploreGaBayesNet.RulesName.get(i).toString();
        		nodeitem.prevRulename=exploreGaBayesNet.RulesName.get(j).toString();
        		nextnode.NodeItems.add(nodeitem);
    		}
    	exploreGaBayesNet.baysNet.Nodes.add(nextnode);
    	
    	
    	String curRulename="";
    	String prevRulename="";
    	
    	
    	//[go-hungry, get-left, go-hungry, get-left]
    	//X0--->X1  for example: path="c0c1c2c3..c(m-1)"  m=chromosome.length, n=exploreGaBayesNet.chroCountForLearnBayesNet
    	//p(X0=r)= #(ci=r)/(n*(m-1)) for i=0..(m-2)
    	//p(X1=q|X0=r)= #(ci=r and c(i+1)=q)/#(ci=r) for i=0..(m-2)
    	
    	
    	
    	
    	for(int k=0;k<=exploreGaBayesNet.baysNet.Nodes.size()-1;k++){
			BaysianNetwork.Node node=exploreGaBayesNet.baysNet.Nodes.get(k);
			for(int r=0;r<=node.NodeItems.size()-1;r++){
				BaysianNetwork.Nodeitem nodeitem=node.NodeItems.get(r);
				curRulename=nodeitem.curRulename;
				prevRulename=nodeitem.prevRulename;
				int count=0;
				if(k==0){///////////Make The First Node (CurrentNode)////prevRulename==""/////////////////////////////////////
					for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1 && i<=exploreGaBayesNet.population.size()-1;i++){
						Chromosome selchromosome=exploreGaBayesNet.population.get(i);
						for(int j=0;j<=selchromosome.ruleNames.size()-2;j++)
		    				if(curRulename.equals(selchromosome.ruleNames.get(j)))
		    					count++;
		 			}
					nodeitem.probability=(double)count/(exploreGaBayesNet.chroCountForLearnBayesNet * (exploreGaBayesNet.DepthOfSearch-1));
				}else {  ////////Make NextNode ////////////
					int count_pre=0;  //#(X0=prevRulename)
					for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
						Chromosome selchromosome=exploreGaBayesNet.population.get(i);
						for(int j=1;j<=selchromosome.ruleNames.size()-1;j++) 
							if(prevRulename.equals(selchromosome.ruleNames.get(j-1))){
								count_pre++;
								if(curRulename.equals(selchromosome.ruleNames.get(j)))
									count++;
							}
		 			}
					nodeitem.probability=(double)count/count_pre; //#(X1=curRulename|X0=prevRulename)/#(X0=prevRulename)
				}
			}  ////end of for
			exploreGaBayesNet.baysNet.Nodes.set(k,node);
    	} ///end of for
		
	}
    private void LearningOfChainBaysianNetwork_chainBOA(ExploringGaBayesNet exploreGaBayesNet){
    	
    	/////exploreGaBayesNet.chroCountForLearnBayesNet For Learning of Bayes Network
    	
    	exploreGaBayesNet.baysNet.Nodes.clear();
    
    	
    	for(int j=0;j<=exploreGaBayesNet.DepthOfSearch-1;j++){
    		 BaysianNetwork.Node node=exploreGaBayesNet.baysNet.getNewNode();
    		 for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
    			 if(j<exploreGaBayesNet.population.get(i).ruleNames.size()){
	    			 String rulename=exploreGaBayesNet.population.get(i).ruleNames.get(j);
	    			 if(!node.AllRulesInNode.contains(rulename))
	    				 node.AllRulesInNode.add(rulename);
    			 }
    		 }
    		 exploreGaBayesNet.baysNet.Nodes.add(node);
    	}
    	
    	for(int k=0;k<=exploreGaBayesNet.baysNet.Nodes.size()-1;k++){
    		if(k==0){/////////////////Make The First Node/////////////////////////////////////////
    			BaysianNetwork.Node node=exploreGaBayesNet.baysNet.Nodes.get(k);
    			for(int r=0;r<=node.AllRulesInNode.size()-1;r++){
    				String rulename=node.AllRulesInNode.get(r);
    				int count=0;
    				for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
    					 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
    						 String rule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
    						 if(rulename.equals(rule_name))
    							 count++;
    					 }
    		    	}
    				Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
    				nodeitem.curRulename=rulename;
    				nodeitem.prevRulename="";
    				nodeitem.probability=(double)count/exploreGaBayesNet.chroCountForLearnBayesNet;
    				node.NodeItems.add(nodeitem);
    			}
    			exploreGaBayesNet.baysNet.Nodes.set(k,node);
    		}else{ ////////////////////Make The Other Nodes////////////////////////////////////////////////
    			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
    			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
    			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
    				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
    					String crulename=curnode.AllRulesInNode.get(c);
    					String prulename=prenode.AllRulesInNode.get(p);
    					int count=0;
    					int allcount=0;
    					for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
    						 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
    							 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
    							 String prule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
    							 if(prulename.equals(prule_name)){
    								 allcount++;
    								 if(crulename.equals(crule_name))
    									 count++;
    							 }
	   		    			 }
    					}
    					Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
        				nodeitem.curRulename=crulename;
        				nodeitem.prevRulename=prulename;
        				if(allcount!=0)
        					nodeitem.probability=(double)count/allcount;
        				else
        					nodeitem.probability=0;
        				curnode.NodeItems.add(nodeitem);
       		    	}
    			}
    			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
    		}  ///////////////////////////////////end of if///////////////////////
    	} //end of for
	}


private void LearningOfBayesianNetwork_tpBOA(ExploringGaBayesNet exploreGaBayesNet){
	
	/////////////In this type of the BOA, each node depends on two previous nodes

	exploreGaBayesNet.baysNet.Nodes.clear();
    
	for(int j=0;j<=exploreGaBayesNet.DepthOfSearch-1;j++){
		 BaysianNetwork.Node node=exploreGaBayesNet.baysNet.getNewNode();
		 for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
			 if(j<exploreGaBayesNet.population.get(i).ruleNames.size()){
    			 String rulename=exploreGaBayesNet.population.get(i).ruleNames.get(j);
    			 if(!node.AllRulesInNode.contains(rulename))
    				 node.AllRulesInNode.add(rulename);
			 }
		 }
		 exploreGaBayesNet.baysNet.Nodes.add(node);
	}
	
	for(int k=0;k<=exploreGaBayesNet.baysNet.Nodes.size()-1;k++){
		if(k==0){/////////////////Make The First Node/////////////////////////////////////////
			BaysianNetwork.Node node=exploreGaBayesNet.baysNet.Nodes.get(k);
			for(int r=0;r<=node.AllRulesInNode.size()-1;r++){
				String rulename=node.AllRulesInNode.get(r);
				int count=0;
				for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
					 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
						 String rule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
						 if(rulename.equals(rule_name))
							 count++;
					 }
		    	}
				Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
				nodeitem.curRulename=rulename;
				nodeitem.prevRulename="";
				nodeitem.probability=(double)count/exploreGaBayesNet.chroCountForLearnBayesNet;
				node.NodeItems.add(nodeitem);
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,node);
		}else if(k==1){/////////////////Make The Second Node/////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					String crulename=curnode.AllRulesInNode.get(c);
					String prulename=prenode.AllRulesInNode.get(p);
					int count=0;
					int allcount=0;
					for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
						 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
							 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
							 String prule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
							 if(prulename.equals(prule_name)){
								 allcount++;
								 if(crulename.equals(crule_name))
									 count++;
							 }
   		    			 }
					}
					Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
    				nodeitem.curRulename=crulename;
    				nodeitem.prevRulename=prulename;
    				if(allcount!=0)
    					nodeitem.probability=(double)count/allcount;
    				else
    					nodeitem.probability=0;
    				curnode.NodeItems.add(nodeitem);
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		}else{ ////////////////////Make The Other Nodes////////////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			BaysianNetwork.Node secondPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-2);
			
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					for(int pp=0;pp<=secondPrenode.AllRulesInNode.size()-1;pp++){
						String crulename=curnode.AllRulesInNode.get(c);
						String prerulename=prenode.AllRulesInNode.get(p);
						String secondPrerulename=secondPrenode.AllRulesInNode.get(pp);
						int count=0;
						int allcount=0;
						for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
							 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
								 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
								 String prerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
								 String secondPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-2);
								 if(prerulename.equals(prerule_name) && secondPrerulename.equals(secondPrerule_name)){
									 allcount++;
									 if(crulename.equals(crule_name))
										 count++;
								 }
	   		    			 }
						}
						Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
	    				nodeitem.curRulename=crulename;
	    				nodeitem.prevRulename=prerulename;
	    				nodeitem.secondPrevRulename=secondPrerulename;
	    				if(allcount!=0)
	    					nodeitem.probability=(double)count/allcount;
	    				else
	    					nodeitem.probability=0;
	    				curnode.NodeItems.add(nodeitem);
					}
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		}  ///////////////////////////////////end of if///////////////////////
	} //end of for

}

private void LearningOfBayesianNetwork_3pBOA(ExploringGaBayesNet exploreGaBayesNet){
	
	/////////////In this type of the BOA, each node depends on two previous nodes

	exploreGaBayesNet.baysNet.Nodes.clear();
    
	for(int j=0;j<=exploreGaBayesNet.DepthOfSearch-1;j++){
		 BaysianNetwork.Node node=exploreGaBayesNet.baysNet.getNewNode();
		 for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
			 if(j<exploreGaBayesNet.population.get(i).ruleNames.size()){
    			 String rulename=exploreGaBayesNet.population.get(i).ruleNames.get(j);
    			 if(!node.AllRulesInNode.contains(rulename))
    				 node.AllRulesInNode.add(rulename);
			 }
		 }
		 exploreGaBayesNet.baysNet.Nodes.add(node);
	}
	
	for(int k=0;k<=exploreGaBayesNet.baysNet.Nodes.size()-1;k++){
		if(k==0){/////////////////Make The First Node/////////////////////////////////////////
			BaysianNetwork.Node node=exploreGaBayesNet.baysNet.Nodes.get(k);
			for(int r=0;r<=node.AllRulesInNode.size()-1;r++){
				String rulename=node.AllRulesInNode.get(r);
				int count=0;
				for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
					 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
						 String rule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
						 if(rulename.equals(rule_name))
							 count++;
					 }
		    	}
				Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
				nodeitem.curRulename=rulename;
				nodeitem.prevRulename="";
				nodeitem.probability=(double)count/exploreGaBayesNet.chroCountForLearnBayesNet;
				node.NodeItems.add(nodeitem);
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,node);
		}else if(k==1){/////////////////Make The Second Node/////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					String crulename=curnode.AllRulesInNode.get(c);
					String prulename=prenode.AllRulesInNode.get(p);
					int count=0;
					int allcount=0;
					for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
						 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
							 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
							 String prule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
							 if(prulename.equals(prule_name)){
								 allcount++;
								 if(crulename.equals(crule_name))
									 count++;
							 }
   		    			 }
					}
					Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
    				nodeitem.curRulename=crulename;
    				nodeitem.prevRulename=prulename;
    				if(allcount!=0)
    					nodeitem.probability=(double)count/allcount;
    				else
    					nodeitem.probability=0;
    				curnode.NodeItems.add(nodeitem);
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		}else if (k==2){ ////////////////////Make The Third Node////////////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			BaysianNetwork.Node secondPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-2);
			
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					for(int pp=0;pp<=secondPrenode.AllRulesInNode.size()-1;pp++){
						String crulename=curnode.AllRulesInNode.get(c);
						String prerulename=prenode.AllRulesInNode.get(p);
						String secondPrerulename=secondPrenode.AllRulesInNode.get(pp);
						int count=0;
						int allcount=0;
						for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
							 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
								 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
								 String prerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
								 String secondPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-2);
								 if(prerulename.equals(prerule_name) && secondPrerulename.equals(secondPrerule_name)){
									 allcount++;
									 if(crulename.equals(crule_name))
										 count++;
								 }
	   		    			 }
						}
						Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
	    				nodeitem.curRulename=crulename;
	    				nodeitem.prevRulename=prerulename;
	    				nodeitem.secondPrevRulename=secondPrerulename;
	    				if(allcount!=0)
	    					nodeitem.probability=(double)count/allcount;
	    				else
	    					nodeitem.probability=0;
	    				curnode.NodeItems.add(nodeitem);
					}
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		}else{ ////////////////////Make The other Nodes////////////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			BaysianNetwork.Node secondPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-2);
			BaysianNetwork.Node thirdPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-3);			
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					for(int pp=0;pp<=secondPrenode.AllRulesInNode.size()-1;pp++){
						for(int ppp=0;ppp<=thirdPrenode.AllRulesInNode.size()-1;ppp++){
							String crulename=curnode.AllRulesInNode.get(c);
							String prerulename=prenode.AllRulesInNode.get(p);
							String secondPrerulename=secondPrenode.AllRulesInNode.get(pp);
							String thirdPrerulename=thirdPrenode.AllRulesInNode.get(ppp);
							int count=0;
							int allcount=0;
							for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
								 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
									 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
									 String prerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
									 String secondPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-2);
									 String thirdPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-3);
									 if(prerulename.equals(prerule_name) && secondPrerulename.equals(secondPrerule_name) && thirdPrerulename.equals(thirdPrerule_name)){
										 allcount++;
										 if(crulename.equals(crule_name))
											 count++;
									 }
		   		    			 }
							}
							Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
		    				nodeitem.curRulename=crulename;
		    				nodeitem.prevRulename=prerulename;
		    				nodeitem.secondPrevRulename=secondPrerulename;
		    				nodeitem.thirdPrevRulename=thirdPrerulename;
		    				if(allcount!=0)
		    					nodeitem.probability=(double)count/allcount;
		    				else
		    					nodeitem.probability=0;
		    	
		    				curnode.NodeItems.add(nodeitem);
						}
					}
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		} ///end of if
	} //end of for

}

private void LearningOfBayesianNetwork_4pBOA(ExploringGaBayesNet exploreGaBayesNet){
	
	/////////////In this type of the BOA, each node depends on two previous nodes

	exploreGaBayesNet.baysNet.Nodes.clear();
    
	for(int j=0;j<=exploreGaBayesNet.DepthOfSearch-1;j++){
		 BaysianNetwork.Node node=exploreGaBayesNet.baysNet.getNewNode();
		 for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
			 if(j<exploreGaBayesNet.population.get(i).ruleNames.size()){
    			 String rulename=exploreGaBayesNet.population.get(i).ruleNames.get(j);
    			 if(!node.AllRulesInNode.contains(rulename))
    				 node.AllRulesInNode.add(rulename);
			 }
		 }
		 exploreGaBayesNet.baysNet.Nodes.add(node);
	}
	
	for(int k=0;k<=exploreGaBayesNet.baysNet.Nodes.size()-1;k++){
		if(k==0){/////////////////Make The First Node/////////////////////////////////////////
			BaysianNetwork.Node node=exploreGaBayesNet.baysNet.Nodes.get(k);
			for(int r=0;r<=node.AllRulesInNode.size()-1;r++){
				String rulename=node.AllRulesInNode.get(r);
				int count=0;
				for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
					 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
						 String rule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
						 if(rulename.equals(rule_name))
							 count++;
					 }
		    	}
				Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
				nodeitem.curRulename=rulename;
				nodeitem.prevRulename="";
				nodeitem.probability=(double)count/exploreGaBayesNet.chroCountForLearnBayesNet;
				node.NodeItems.add(nodeitem);
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,node);
		}else if(k==1){/////////////////Make The Second Node/////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					String crulename=curnode.AllRulesInNode.get(c);
					String prulename=prenode.AllRulesInNode.get(p);
					int count=0;
					int allcount=0;
					for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
						 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
							 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
							 String prule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
							 if(prulename.equals(prule_name)){
								 allcount++;
								 if(crulename.equals(crule_name))
									 count++;
							 }
   		    			 }
					}
					Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
    				nodeitem.curRulename=crulename;
    				nodeitem.prevRulename=prulename;
    				if(allcount!=0)
    					nodeitem.probability=(double)count/allcount;
    				else
    					nodeitem.probability=0;
    				curnode.NodeItems.add(nodeitem);
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		}else if (k==2){ ////////////////////Make The Third Node////////////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			BaysianNetwork.Node secondPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-2);
			
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					for(int pp=0;pp<=secondPrenode.AllRulesInNode.size()-1;pp++){
						String crulename=curnode.AllRulesInNode.get(c);
						String prerulename=prenode.AllRulesInNode.get(p);
						String secondPrerulename=secondPrenode.AllRulesInNode.get(pp);
						int count=0;
						int allcount=0;
						for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
							 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
								 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
								 String prerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
								 String secondPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-2);
								 if(prerulename.equals(prerule_name) && secondPrerulename.equals(secondPrerule_name)){
									 allcount++;
									 if(crulename.equals(crule_name))
										 count++;
								 }
	   		    			 }
						}
						Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
	    				nodeitem.curRulename=crulename;
	    				nodeitem.prevRulename=prerulename;
	    				nodeitem.secondPrevRulename=secondPrerulename;
	    				nodeitem.probability=(double)count/allcount;
	    				curnode.NodeItems.add(nodeitem);
					}
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		}else if (k==3) { ////////////////////Make The Fourth Node////////////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			BaysianNetwork.Node secondPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-2);
			BaysianNetwork.Node thirdPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-3);
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					for(int pp=0;pp<=secondPrenode.AllRulesInNode.size()-1;pp++){
						for(int ppp=0;ppp<=thirdPrenode.AllRulesInNode.size()-1;ppp++){
							String crulename=curnode.AllRulesInNode.get(c);
							String prerulename=prenode.AllRulesInNode.get(p);
							String secondPrerulename=secondPrenode.AllRulesInNode.get(pp);
							String thirdPrerulename=thirdPrenode.AllRulesInNode.get(ppp);
							int count=0;
							int allcount=0;
							for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
								 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
									 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
									 String prerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
									 String secondPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-2);
									 String thirdPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-3);
									 if(prerulename.equals(prerule_name) && secondPrerulename.equals(secondPrerule_name) && thirdPrerulename.equals(thirdPrerule_name) ){
										 allcount++;
										 if(crulename.equals(crule_name))
											 count++;
									 }
								 }
		   		    		}
							Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
		    				nodeitem.curRulename=crulename;
		    				nodeitem.prevRulename=prerulename;
		    				nodeitem.secondPrevRulename=secondPrerulename;
		    				nodeitem.thirdPrevRulename=thirdPrerulename;
		    				nodeitem.probability=(double)count/allcount;
		    				curnode.NodeItems.add(nodeitem);
						}
					}
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		}else  { ////////////////////Make The other Nodes////////////////////////////////////////////////
			BaysianNetwork.Node curnode=exploreGaBayesNet.baysNet.Nodes.get(k);
			BaysianNetwork.Node prenode=exploreGaBayesNet.baysNet.Nodes.get(k-1);
			BaysianNetwork.Node secondPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-2);
			BaysianNetwork.Node thirdPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-3);
			BaysianNetwork.Node fourthPrenode=exploreGaBayesNet.baysNet.Nodes.get(k-4);
			for(int c=0;c<=curnode.AllRulesInNode.size()-1;c++){
				for(int p=0;p<=prenode.AllRulesInNode.size()-1;p++){
					for(int pp=0;pp<=secondPrenode.AllRulesInNode.size()-1;pp++){
						for(int ppp=0;ppp<=thirdPrenode.AllRulesInNode.size()-1;ppp++){
							for(int pppp=0;pppp<=fourthPrenode.AllRulesInNode.size()-1;pppp++){
								String crulename=curnode.AllRulesInNode.get(c);
								String prerulename=prenode.AllRulesInNode.get(p);
								String secondPrerulename=secondPrenode.AllRulesInNode.get(pp);
								String thirdPrerulename=thirdPrenode.AllRulesInNode.get(ppp);
								String fourthPrerulename=fourthPrenode.AllRulesInNode.get(pppp);
								int count=0;
								int allcount=0;
								for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
									 if(k<exploreGaBayesNet.population.get(i).ruleNames.size()){
										 String crule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k);
										 String prerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-1);
										 String secondPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-2);
										 String thirdPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-3);
										 String fourthPrerule_name=exploreGaBayesNet.population.get(i).ruleNames.get(k-4);
										 if(prerulename.equals(prerule_name) && secondPrerulename.equals(secondPrerule_name) && thirdPrerulename.equals(thirdPrerule_name) &&  fourthPrerulename.equals(fourthPrerule_name)){
											 allcount++;
											 if(crulename.equals(crule_name))
												 count++;
										 }
			   		    			 }
								}
								Nodeitem nodeitem=exploreGaBayesNet.baysNet.getNewNodeitem();
			    				nodeitem.curRulename=crulename;
			    				nodeitem.prevRulename=prerulename;
			    				nodeitem.secondPrevRulename=secondPrerulename;
			    				nodeitem.thirdPrevRulename=thirdPrerulename;
			    				nodeitem.fourthPrevRulename=fourthPrerulename;
			    				nodeitem.probability=(double)count/allcount;
			    				curnode.NodeItems.add(nodeitem);
							}
						}
					}
   		    	}
			}
			exploreGaBayesNet.baysNet.Nodes.set(k,curnode);
		}///end of if
	} //end of for

}




	
	public void Mutate(ExploringGaBayesNet exploreGaBayesNet,double probability)
    {
		ArrayList<Chromosome> offsprings=new ArrayList<Chromosome>(); 

		 for (int i = 0; i < exploreGaBayesNet.population.size(); i++){
			 Chromosome offspring = exploreGaBayesNet.population.get(i);
	         for (int mutatePosition = 0; mutatePosition <= exploreGaBayesNet.DepthOfSearch-1; mutatePosition++)
	         {
	                if (Assay(probability)) //if the chance is to mutate
	                {
	                    int newGene = (int)(GetRandomVal(0,exploreGaBayesNet.maxValueInAllChromosomes ));
	                    if(mutatePosition<=offspring.genes.size()-1)
	                    	offspring.genes.set(mutatePosition,newGene);
	                    
	                }
	          }
	         offsprings.add(offspring);
		 }
		 
		 exploreGaBayesNet.population = offsprings;
         
   }
	
	
    private int GetRandomVal(int min, int max)
    {
         return  (int) (min + Math.random() * (max - min));
   }
	
	private boolean Assay(double probability)
    {
        if (Math.random()< probability)
            return true;
        else
            return false;
    }
		
	private void sortPopulation(ExploringGaBayesNet exploreGaBayesNet){
		///////////////////////////////bubble sort///
		///sort based on fitness
		///if (exploreGaBayesNet.ModelCheckingTarget.equals("DeadLock")) then Incremental Sorting
		///if(!exploreGaBayesNet.ModelCheckingTarget.equals("DeadLock")) then Decremental Sorting
		///////////////
		////////////////////////////////////////////
		///according to the selection type of GA,BOA,.... we will sort.
		///1) Trucation (TRUNC): by default --the objective chromosomes are in the correct positions
		///2) Tournament (TOUR): select some chromosomes and take them in the correct positions.
		/////////////////////////////////////////////
		if(exploreGaBayesNet.ModelCheckingTarget.equals("DeadLock")){
			boolean swapped = true;
			int j = 0;
			ExploringGaBayesNet.Chromosome  tmp;
			while (swapped){
				swapped = false;
				j++;
				for (int i = 0; i < exploreGaBayesNet.population.size() - j; i++) {
					if (exploreGaBayesNet.population.get(i).fitness > exploreGaBayesNet.population.get(i+1).fitness) {
					    tmp = exploreGaBayesNet.population.get(i);
					    exploreGaBayesNet.population.set(i, exploreGaBayesNet.population.get(i+1));
					    exploreGaBayesNet.population.set(i+1,tmp);
					    swapped = true;
					}
				}
			}

		}else{
			boolean swapped = true;
			int j = 0;
			ExploringGaBayesNet.Chromosome  tmp;
			while (swapped){
				swapped = false;
				j++;
				for (int i = 0; i < exploreGaBayesNet.population.size() - j; i++) {
					if (exploreGaBayesNet.population.get(i).fitness < exploreGaBayesNet.population.get(i+1).fitness) {
					    tmp = exploreGaBayesNet.population.get(i);
					    exploreGaBayesNet.population.set(i, exploreGaBayesNet.population.get(i+1));
					    exploreGaBayesNet.population.set(i+1,tmp);
					    swapped = true;
					}
				}
			}

		}
		////////////////////////////////////////////
		
		if(SelectionType.equals("TOUR")){
				int tourSize=4;
				ArrayList<Integer> AselChro=new ArrayList<Integer>();
				for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet;i++){
					int indexOfMaxFitness=-1;
					int x=(int)(GetRandomVal(0,exploreGaBayesNet.population.size()));
					indexOfMaxFitness=x;
					for(int j=1;j<=tourSize-1;j++){
						x=(int)(GetRandomVal(0,exploreGaBayesNet.population.size()));
						if(exploreGaBayesNet.ModelCheckingTarget.equals("DeadLock")){
							if(exploreGaBayesNet.population.get(x).fitness<exploreGaBayesNet.population.get(indexOfMaxFitness).fitness)
								indexOfMaxFitness=x;
						}else{
							if(exploreGaBayesNet.population.get(x).fitness>exploreGaBayesNet.population.get(indexOfMaxFitness).fitness)
								indexOfMaxFitness=x;
						}
											
					}
					AselChro.add(indexOfMaxFitness);
				}
				
				for(int i=0;i<=exploreGaBayesNet.chroCountForLearnBayesNet-1;i++){
					///exchange the chro's of i and AselChro[i]
					int p=AselChro.get(i);
					ExploringGaBayesNet.Chromosome tmp = exploreGaBayesNet.population.get(i);
				    exploreGaBayesNet.population.set(i, exploreGaBayesNet.population.get(p));
				    exploreGaBayesNet.population.set(p,tmp);
				}
		}
		
		
	}
	private void createInitialPopulation_BOA(ExploringGaBayesNet exploreGaBayesNet,ExploreType exploreType){
    	if(!callFromHeuGenerator){
    		simulator.getModel().resetGTS();
    		exploreType=exploreGaBayesNet.simulator.getModel().getExploreType();
    	}else{
    		GTS gts=null;
			try {
				gts = new GTS(exploreGaBayesNet.grammer);
			} catch (FormatException e) {
				// do nothing
				e.printStackTrace();
			}
			exploreGaBayesNet.gts=gts;
			exploreGaBayesNet.initialState=gts.startState();
    	}
    	HeuBOAExploreAction heuExploreAction=new HeuBOAExploreAction(simulator, false);
		heuExploreAction.explore(exploreType, exploreGaBayesNet);
		heuExploreAction=null;
		
		
	}
	 
	public Simulator simulator;
	public String HostGraphName;
	public String ModelCheckingType;
	public String ModelCheckingTarget;
	public String CTLproperty;
	
	public  int CountOFpopulation;
	public  int Iterations;
	public  int DepthOfSearch;
	public  double MutationRate;
	public  double CrossOverRate;
	
	public  double SelectionRate;
	public  double ReplacementRate;
	
	
	
	
	
	
	/**
	 * Save all paths from final state's to initial state.
	*/
	
	public String SelectionType;
	public ExploringGaBayesNet exploreGaBayesNet;
	public Boolean callFromHeuGenerator=false;
	public long Number_Explored_States;
	public long First_Found_Dead_depth; //The first found deadlock depth
	public long First_Found_Dead_Rep;  //The first deadlock is found after how many repetitions
	public long Call_Number_Fitness;  //The number of fitness function calls
	public long RunningTime_AllFitnessFuncs;  //The running time of all fitness function calls
	
	public int timeLimit;
	public boolean isContinue=false;
	public long lastTime=0;
	
	//public HeuristicGAExploreAction heuExploreAction;
}

		