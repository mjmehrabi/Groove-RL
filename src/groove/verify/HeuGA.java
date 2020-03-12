package groove.verify;



import groove.grammar.Condition;
import groove.grammar.Grammar;
import groove.grammar.QualName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
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
import groove.gui.action.HeuGAExploreAction;
//import groove.gui.action.HeuGAExploreAction;
import groove.gui.display.DisplayKind;
import groove.gui.display.LTSDisplay;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.util.parse.FormatException;
import groove.verify.ExploringGaBayesNet.Chromosome;

/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */


public class HeuGA{
	public HeuGA(){
		exploreGaBayesNet=new ExploringGaBayesNet();
	}
	
	public void EnableSelectedHostGraph(){
        try {
            simulator.getModel().doEnableUniquely(ResourceKind.HOST,QualName.name(HostGraphName));
        } catch (IOException exc) {
        	System.err.println("Error during %s enabling" +ResourceKind.HOST.getDescription());
        }
        
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public String start(String targetRule,String GAType,String SelectionType,int RulesCount,ArrayList<QualName> RulesName,Grammar grammer,GrammarModel grammermodel){
		
		
		
	
		
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
		        			try {
								if(rulemodel.toResource().getAnchor().size()>0)
										flag=true;
							} catch (FormatException e) {
								// TODO Auto-generated catch block
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
					// TODO Auto-generated catch block
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
							// TODO Auto-generated catch block
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
			exploreGaBayesNet.GAType=GAType;
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
    	        			try {
								if(rulemodel.toResource().getAnchor().size()>0)
									flag=true;
							} catch (FormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
   	        			
    	        		}
    	        		
    	        		if(!flag)
    	        			Alltype.add(ts);
    	        	}
    	       	}

           	exploreGaBayesNet.Alltype=Alltype;
           	
			////////////////////////////
          	
           			
			
			exploreGaBayesNet.isProgressVisible=false;
			if(callFromHeuGenerator)
				exploreGaBayesNet.isProgressVisible=false;
			exploreGaBayesNet.WhatStep="CIP";  //createInitialPopulation
			
			
			///////////////GA/////////////GA///////////////GA/////////////////////////////////
			////////////GA/////////////GA///////////////GA/////////////////////////////////////////////
			////////////GA/////////////GA///////////////GA/////////////////////////////////////////////
			if(exploreGaBayesNet.GAType.equals("GA")){ //Genetic Algorithm
				createInitialPopulation_GA(exploreGaBayesNet,exploreType);
				
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
        				createInitialPopulation_GA(exploreGaBayesNet,exploreType);
    				}
	    		}
				
				
				if(exploreGaBayesNet.heuristicResult==null) 
					sortPopulation(exploreGaBayesNet);
				if(!exploreGaBayesNet.ModelCheckingTarget.equals("DeadLock") && exploreGaBayesNet.heuristicResult==null ){ ////Reachability
					for (int iter = 0; iter <= exploreGaBayesNet.Iterations-1 && exploreGaBayesNet.heuristicResult==null ; iter++){
						 exploreGaBayesNet.First_Found_Dead_Rep++; 
						 Crossover(exploreGaBayesNet,exploreGaBayesNet.CrossOverRate);
			    		 Mutate(exploreGaBayesNet,exploreGaBayesNet.MutationRate);
			    		 exploreGaBayesNet.WhatStep="CFN";  //CalcFitness
			    		 CalcFitness(exploreGaBayesNet,exploreType);
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
					for (int iter = 0; iter <= exploreGaBayesNet.Iterations-1 && exploreGaBayesNet.heuristicResult==null ; iter++){
						 exploreGaBayesNet.First_Found_Dead_Rep++; 
						 Crossover(exploreGaBayesNet,exploreGaBayesNet.CrossOverRate);
			    		 Mutate(exploreGaBayesNet,exploreGaBayesNet.MutationRate);
			    		 exploreGaBayesNet.WhatStep="CFN";  //CalcFitness
			    		 CalcFitness(exploreGaBayesNet,exploreType);
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
	
	
	private void createInitialPopulation_GA(ExploringGaBayesNet ExploringGaBayesNet,ExploreType exploreType){
		
    	int CountOFpopulation=ExploringGaBayesNet.CountOFpopulation;
    	int chroIndex=0;
    	
    	
		while(chroIndex<CountOFpopulation && ExploringGaBayesNet.heuristicResult==null){
			if(!callFromHeuGenerator){
	    		simulator.getModel().resetGTS();
	    		exploreType=exploreGaBayesNet.simulator.getModel().getExploreType();
	    	}else{
	    		GTS gts=null;
				try {
					gts = new GTS(exploreGaBayesNet.grammer);
				} catch (FormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				exploreGaBayesNet.gts=gts;
				exploreGaBayesNet.initialState=gts.startState();
	    	}
			HeuGAExploreAction heuExploreAction=new HeuGAExploreAction(simulator, false);
			heuExploreAction.explore(exploreType, exploreGaBayesNet);
			chroIndex++;
 	    }
    	    
	}
   
////////////////////////////////////////////////////////////////////////////
	private void CalcFitness(ExploringGaBayesNet exploreGaBayesNet,ExploreType exploreType ){
		
    	int CountOFpopulation=exploreGaBayesNet.CountOFpopulation;
    	int chroIndex=0;
    	exploreGaBayesNet.totalFitness=0;
 	    while(chroIndex<CountOFpopulation && exploreGaBayesNet.heuristicResult==null){
 	    	if(!callFromHeuGenerator){
 	    		simulator.getModel().resetGTS();
 	    		exploreType=exploreGaBayesNet.simulator.getModel().getExploreType();
 	    		exploreGaBayesNet.exploreType=exploreType;
 	    	}else{
 	    		GTS gts=null;
				try {
					gts = new GTS(exploreGaBayesNet.grammer);
				} catch (FormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 				exploreGaBayesNet.gts=gts;
 				exploreGaBayesNet.initialState=gts.startState();
 	    	}
 	    	exploreGaBayesNet.chroIndex=chroIndex;
			HeuGAExploreAction heuExploreAction=new HeuGAExploreAction(simulator, false);
			heuExploreAction.explore(exploreType, exploreGaBayesNet);
			heuExploreAction=null;
 	    		    	 	    	
 	    	
			chroIndex++;
 	    }
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
	
	private void Crossover(ExploringGaBayesNet exploreGaBayesNet,double probability){
		ArrayList<Chromosome> offspring=new ArrayList<Chromosome>(); 
		
        for (int i = 0; i < exploreGaBayesNet.population.size(); i++)
        {
           if (Assay(probability)) //if the chance is to crossover
           {
        	   int x=(int)(GetRandomVal(0,exploreGaBayesNet.chroCountForLearnBayesNet-1));
               Chromosome parentX = exploreGaBayesNet.population.get(x);
               int y=(int)(GetRandomVal(0,exploreGaBayesNet.chroCountForLearnBayesNet-1));
               Chromosome parentY = exploreGaBayesNet.population.get(y);

               ArrayList<Integer> child=new ArrayList<Integer>();
               for (int j = 0; j <=exploreGaBayesNet.DepthOfSearch-1; j++)
               {
                   if (Assay(0.5)) //select from parentX
                   {
                	   if(j<parentX.genes.size())
                		   child.add(parentX.genes.get(j));
                	   else
                		   child.add((int)(GetRandomVal(0,exploreGaBayesNet.maxValueInAllChromosomes )));
                		   
                   }
                   else //select from parentY
                   {	
                	   if(j<parentY.genes.size())
                		   child.add(parentY.genes.get(j));
                	   else
                		   child.add((int)(GetRandomVal(0,exploreGaBayesNet.maxValueInAllChromosomes )));
                		   
                   }
               }
               ExploringGaBayesNet.Chromosome offSpr=exploreGaBayesNet.getNewChromosome();
               offSpr.genes = child;
               offspring.add(offSpr);

           }
           else //else the chance is to clonning
           {
        	   int x=(int)(GetRandomVal(0,exploreGaBayesNet.chroCountForLearnBayesNet-1));
               Chromosome parentX = exploreGaBayesNet.population.get(x);
               offspring.add(parentX);
           }
       }

       while (offspring.size() > exploreGaBayesNet.population.size())
       {
           offspring.remove((int)GetRandomVal(0, offspring.size() - 1));
       }

       exploreGaBayesNet.population = offspring;
       if(exploreGaBayesNet.population.size()>50)
    	   offspring=null;
	}
    private int GetRandomVal(int min, int max)
    {
         return  (int) (min + Math.random() * (max - min));
   }
	private Chromosome AssayRuletteWheel(ExploringGaBayesNet exploreGaBayesNet)
    {
		
        Chromosome selection = exploreGaBayesNet.population.get(0);
        double probability = Math.random();
        for (int i = 0; i < exploreGaBayesNet.population.size(); i++)
        {
            selection = exploreGaBayesNet.population.get(i);
            if (exploreGaBayesNet.population.get(i).cumAvgFitness > probability)
                break;

        }
        return selection;
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
		///according to the selection type of GA,.... we will sort.
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

		