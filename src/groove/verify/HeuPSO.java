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

import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.model.RuleModel;
import groove.grammar.rule.RuleEdge;
import groove.grammar.rule.RuleNode;
import groove.gui.Simulator;
import groove.gui.SimulatorModel;
import groove.gui.action.HeuPSOExploreAction;


import groove.gui.display.DisplayKind;
import groove.gui.display.LTSDisplay;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.util.parse.FormatException;


/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */


public class HeuPSO{
	public HeuPSO(){
		exploringItemPSO=new ExploringItemPSO();
	}
	
	public void EnableSelectedHostGraph(){
        try {
            simulator.getModel().doEnableUniquely(ResourceKind.HOST, QualName.name(HostGraphName));
        } catch (IOException exc) {
        	System.err.println("Error during %s enabling" +ResourceKind.HOST.getDescription());
        }
        
	}
	
	public String start(String targetRule,int RulesCount,ArrayList<QualName> RulesName,Grammar grammer,GrammarModel grammermodel){
		
		
		
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
		
		
		
			exploringItemPSO=new ExploringItemPSO();
			exploringItemPSO.callFromHeuGenerator=callFromHeuGenerator;
			exploringItemPSO.grammer=grammer;
			exploringItemPSO.grammermodel=grammermodel;
			exploringItemPSO.CTLproperty=CTLproperty;   //"DeadLock" "Reachability"
			exploringItemPSO.ModelCheckingTarget=ModelCheckingTarget; 
			exploringItemPSO.targetRule=ModelCheckingTarget;
			
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
					e.printStackTrace();
				}
				exploringItemPSO.gts=gts;
				exploringItemPSO.initialState=gts.startState();
				exploringItemPSO.exploreType=exploreType;
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
							e.printStackTrace();
						}
                		
                		Collection<Condition> allcond=condition.getSubConditions();
                		Set<RuleEdge> patEdgeSet=condition.getPattern().edgeSet();
                		for(RuleEdge re:patEdgeSet){
                			exploringItemPSO.targetGraph_edgeList.add(re);
                			if(!exploringItemPSO.targetGraph_nodeList.contains(re.source()))
                				exploringItemPSO.targetGraph_nodeList.add(re.source());
                			if(!exploringItemPSO.targetGraph_nodeList.contains(re.target()))
                				exploringItemPSO.targetGraph_nodeList.add(re.target());
                		}
                		for(Condition cond : allcond){
                			ExploringItemPSO.NAC nac=exploringItemPSO.getNewNAC();
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
                			exploringItemPSO.allNACs.add(nac);
                		}
                		break;
            		}
           	}
						
			//////////////////////////////////////////
			//////////////////////////////////////////

           	exploringItemPSO.RulesCount=RulesCount;
           	exploringItemPSO.RulesName=RulesName;
			
			
			exploringItemPSO.CountOFpopulation=CountOFpopulation;
			exploringItemPSO.Iterations=Iterations;
			exploringItemPSO.DepthOfSearch=DepthOfSearch+1;
			exploringItemPSO.C1=C1;
			exploringItemPSO.C2=C2;
			exploringItemPSO.W=W;
			
			exploringItemPSO.targetRule=targetRule;
			exploringItemPSO.ModelCheckingTarget=ModelCheckingTarget;
			exploringItemPSO.simulator=simulator;
			exploringItemPSO.psoType=psoType;
			
			exploringItemPSO.Number_Explored_States=0;
			exploringItemPSO.First_Found_Dead_depth=0;
			exploringItemPSO.First_Found_Dead_Rep=0;
			exploringItemPSO.Call_Number_Fitness=0;
			
						
	    	
			////////////////////////////////////
			ArrayList<QualName> Alltype=new ArrayList<QualName>();
    		
        	//grammermodel=exploringItemPSO.simulator.getModel().getGrammar();
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
    	        				e.printStackTrace();
    	        			}
    	        		}
    	        		
    	        		if(!flag)
    	        			Alltype.add(ts);
    	        	}
    	       	}

           	exploringItemPSO.Alltype=Alltype;
           	
		 	
           			
			
			exploringItemPSO.isProgressVisible=false;
			exploringItemPSO.WhatStep="CIP";  //createInitialPopulation
			
			
			
			if(exploringItemPSO.psoType.equals("PSO")){  /////////////PSO
				createInitialPopulation(exploringItemPSO,exploreType);
				if(exploringItemPSO.heuristicResult==null){ 
					sortPopulation(exploringItemPSO);
					copy_AllgenesTolocs(exploringItemPSO);
					exploringItemPSO.pBestLocation=new ExploringItemPSO.Location[exploringItemPSO.CountOFpopulation];
					exploringItemPSO.pBest=new double[exploringItemPSO.CountOFpopulation];
					for(int i=0; i<exploringItemPSO.CountOFpopulation; i++) {
						exploringItemPSO.pBest[i] = exploringItemPSO.population.get(i).fitness;
						ExploringItemPSO.Location location=exploringItemPSO.getNewLocation();
						location.loc=new int[exploringItemPSO.population.get(i).genes.size()];
						for(int j=0;j<=exploringItemPSO.population.get(i).genes.size()-1;j++)
							location.loc[j]=exploringItemPSO.population.get(i).genes.get(j);
						
						exploringItemPSO.pBestLocation[i]=location;
					}
				}
				if(!exploringItemPSO.ModelCheckingTarget.equals("DeadLock") && exploringItemPSO.heuristicResult==null ){ ////Reachability
					for (int iter = 0; iter <= exploringItemPSO.Iterations-1 && exploringItemPSO.heuristicResult==null ; iter++){
						copy_AllgenesTolocs(exploringItemPSO);
						// step 1 - update pBest
						Update_pBest(exploringItemPSO);
						// step 2 - update gBest
						Update_gBest(exploringItemPSO,iter);
						//step3 -update velocity and locations
						Update_velocity_location(exploringItemPSO,iter,exploringItemPSO.Iterations);
						
						exploringItemPSO.First_Found_Dead_Rep++; 
						exploringItemPSO.WhatStep="CFN";  //CalcFitness
			    		 CalcFitness(exploringItemPSO,exploreType);
			    		 if(exploringItemPSO.heuristicResult==null) 
			    			 sortPopulation(exploringItemPSO);
				    }	
	    		
				}else{
					for (int iter = 0; iter <= exploringItemPSO.Iterations-1 && exploringItemPSO.heuristicResult==null ; iter++){
						copy_AllgenesTolocs(exploringItemPSO);
						// step 1 - update pBest
						Update_pBest(exploringItemPSO);
						// step 2 - update gBest
						Update_gBest(exploringItemPSO,iter);
						//step3 -update velocity and locations
						Update_velocity_location(exploringItemPSO,iter,exploringItemPSO.Iterations);
						
						 exploringItemPSO.First_Found_Dead_Rep++; 
						 exploringItemPSO.WhatStep="CFN";  //CalcFitness
			    		 CalcFitness(exploringItemPSO,exploreType);
			    		 if(exploringItemPSO.heuristicResult==null) 
			    			 sortPopulation(exploringItemPSO);
				    }
				}

			}else{   ///////////////////////////////PSO-GSA
				createInitialPopulation_GSA(exploringItemPSO,exploreType);
				if(exploringItemPSO.heuristicResult==null){ 
					sortPopulation(exploringItemPSO);
					copy_AllgenesTolocs(exploringItemPSO);
					exploringItemPSO.pBestLocation=new ExploringItemPSO.Location[exploringItemPSO.CountOFpopulation];
					exploringItemPSO.pBest=new double[exploringItemPSO.CountOFpopulation];
					for(int i=0; i<exploringItemPSO.CountOFpopulation; i++) {
						exploringItemPSO.pBest[i] = exploringItemPSO.population.get(i).fitness;
						ExploringItemPSO.Location location=exploringItemPSO.getNewLocation();
						location.loc=new int[exploringItemPSO.population.get(i).genes.size()];
						for(int j=0;j<=exploringItemPSO.population.get(i).genes.size()-1;j++)
							location.loc[j]=exploringItemPSO.population.get(i).genes.get(j);
						
						exploringItemPSO.pBestLocation[i]=location;
					}
				}
				if(!exploringItemPSO.ModelCheckingTarget.equals("DeadLock") && exploringItemPSO.heuristicResult==null ){ ////Reachability
					for (int iter = 0; iter <= exploringItemPSO.Iterations-1 && exploringItemPSO.heuristicResult==null ; iter++){
												
						exploringItemPSO.G=1*Math.exp(-23*iter/exploringItemPSO.Iterations);
						
						copy_AllgenesTolocs(exploringItemPSO);
						Update_pBest(exploringItemPSO);
						Update_gBest(exploringItemPSO,iter);
						Calculate_Mass(exploringItemPSO);
						Force_Update(exploringItemPSO);
						Acc_Velocity_Loc_Update(exploringItemPSO);
						
						exploringItemPSO.First_Found_Dead_Rep++; 
						exploringItemPSO.WhatStep="CFN";  //CalcFitness
			    		 CalcFitness(exploringItemPSO,exploreType);
			    		 if(exploringItemPSO.heuristicResult==null) 
			    			 sortPopulation(exploringItemPSO);
				    }	
	    		
				}else{
					for (int iter = 0; iter <= exploringItemPSO.Iterations-1 && exploringItemPSO.heuristicResult==null ; iter++){

						exploringItemPSO.G=1*Math.exp(-23*iter/exploringItemPSO.Iterations);
						
						copy_AllgenesTolocs(exploringItemPSO);
						Update_pBest(exploringItemPSO);
						Update_gBest(exploringItemPSO,iter);
						Calculate_Mass(exploringItemPSO);
						Force_Update(exploringItemPSO);
						Acc_Velocity_Loc_Update(exploringItemPSO);

						 exploringItemPSO.First_Found_Dead_Rep++; 
						 exploringItemPSO.WhatStep="CFN";  //CalcFitness
			    		 CalcFitness(exploringItemPSO,exploreType);
			    		 if(exploringItemPSO.heuristicResult==null) 
			    			 sortPopulation(exploringItemPSO);
				    }
				}

			}
				
			
			
		

			if(!callFromHeuGenerator==true)
				if(exploringItemPSO.heuristicResult==null)
					simulator.getModel().resetGTS();
			
			if(exploringItemPSO.heuristicResult==null)
				exploringItemPSO.heuristicResult="noreachability";
       	    
			////////////////////////////////
			///////////////////////////////
			if(callFromHeuGenerator==true){
				if(exploringItemPSO.heuristicResult.equals("reachability")){
					ExploringItemPSO.Particle particle=exploringItemPSO.population.get(exploringItemPSO.partIndexCounterExamlpe);
					return "The property is verified."+" Target state found in depth:"+ exploringItemPSO.First_Found_Dead_depth +" The number of explored states:"+exploringItemPSO.Number_Explored_States +" The number of fitness calls:"+exploringItemPSO.Call_Number_Fitness+" ";
				}
				else
					return "The property is not verified.";
			}
			////////////////////////////////
			///////////////////////////////
			
			
			if(exploringItemPSO.heuristicResult.equals("reachability")){
				LTSDisplay ltsDisplay=(LTSDisplay)exploringItemPSO.simulator.getDisplaysPanel().getDisplay(DisplayKind.LTS);
				ArrayList<GraphState> result=new ArrayList<GraphState>();
				
				result.add(exploringItemPSO.simulator.getModel().getGTS().startState());
				ExploringItemPSO.Particle particle=exploringItemPSO.population.get(exploringItemPSO.partIndexCounterExamlpe);
				for(int i=0;i<=particle.states.size()-1;i++)
					result.add(particle.states.get(i));
				//result.add(exploringItemPSO.lastStateInReachability);
				ltsDisplay.emphasiseStates(result, true);
			}
				
			Number_Explored_States=exploringItemPSO.Number_Explored_States;
			First_Found_Dead_depth=exploringItemPSO.First_Found_Dead_depth-1;
			First_Found_Dead_Rep=exploringItemPSO.First_Found_Dead_Rep;
			Call_Number_Fitness=exploringItemPSO.Call_Number_Fitness;
			
			
			if(exploringItemPSO.heuristicResult.equals("reachability"))
				return exploringItemPSO.heuristicResult+"_"+exploringItemPSO.lastStateInReachability.toString();
			else
				return exploringItemPSO.heuristicResult;
	    
	}
	private void Update_pBest(ExploringItemPSO exploringItemPSO){
		if(exploringItemPSO.CTLproperty.equals("Reachability")){
			for(int i=0; i<exploringItemPSO.CountOFpopulation; i++) {
				if(exploringItemPSO.population.get(i).fitness > exploringItemPSO.pBest[i]) {
 					exploringItemPSO.pBest[i] = exploringItemPSO.population.get(i).fitness;
					ExploringItemPSO.Location location=exploringItemPSO.getNewLocation();
					location.loc=new int[exploringItemPSO.population.get(i).genes.size()];
					for(int j=0;j<=exploringItemPSO.population.get(i).genes.size()-1;j++)
						location.loc[j]=exploringItemPSO.population.get(i).genes.get(j);
					exploringItemPSO.pBestLocation[i]=location;
				}
			}
		}else{   //deadlock
			for(int i=0; i<exploringItemPSO.CountOFpopulation; i++) {
				if(exploringItemPSO.population.get(i).fitness < exploringItemPSO.pBest[i]) {
					exploringItemPSO.pBest[i] = exploringItemPSO.population.get(i).fitness;
					ExploringItemPSO.Location location=exploringItemPSO.getNewLocation();
					location.loc=new int[exploringItemPSO.population.get(i).genes.size()];
					for(int j=0;j<=exploringItemPSO.population.get(i).genes.size()-1;j++)
						location.loc[j]=exploringItemPSO.population.get(i).genes.get(j);
					exploringItemPSO.pBestLocation[i]=location;
				}
			}
		}
		
		
	}
	private void Update_gBest(ExploringItemPSO exploringItemPSO,int rep){
		int bestParticleIndex = IndexofPart_Bestfitness(exploringItemPSO);
		if(exploringItemPSO.CTLproperty.equals("Reachability")){
			if(rep == 0 || exploringItemPSO.population.get(bestParticleIndex).fitness > exploringItemPSO.gBest) {
				exploringItemPSO.gBest = exploringItemPSO.population.get(bestParticleIndex).fitness;
				ExploringItemPSO.Location location=exploringItemPSO.getNewLocation();
				location.loc=new int[exploringItemPSO.population.get(bestParticleIndex).genes.size()];
				for(int j=0;j<=exploringItemPSO.population.get(bestParticleIndex).genes.size()-1;j++)
					location.loc[j]=exploringItemPSO.population.get(bestParticleIndex).genes.get(j);
				exploringItemPSO.gBestLocation=location;
			}		

		}else{ //deadlock
			if(rep == 0 || exploringItemPSO.population.get(bestParticleIndex).fitness < exploringItemPSO.gBest) {
				exploringItemPSO.gBest = exploringItemPSO.population.get(bestParticleIndex).fitness;
				ExploringItemPSO.Location location=exploringItemPSO.getNewLocation();
				location.loc=new int[exploringItemPSO.population.get(bestParticleIndex).genes.size()];
				for(int j=0;j<=exploringItemPSO.population.get(bestParticleIndex).genes.size()-1;j++)
					location.loc[j]=exploringItemPSO.population.get(bestParticleIndex).genes.get(j);
				exploringItemPSO.gBestLocation=location;
			}		

		}
		
	}
	private void Update_velocity_location(ExploringItemPSO exploringItemPSO,int t,int MAX_ITERATION){
		double w = 0.9 - (((double) t) / MAX_ITERATION) * (0.9 - 0.4);
		for(int i=0; i<exploringItemPSO.CountOFpopulation; i++) {
			double r1 = Math.random();
			double r2 = Math.random();
			ExploringItemPSO.Particle p = exploringItemPSO.population.get(i);
			
			// update velocity
			double[] newVel = new double[p.genes.size()];
			//if(p.velocity.vel.length<p.genes.size() || exploringItemPSO.pBestLocation.length<exploringItemPSO.CountOFpopulation || exploringItemPSO.pBestLocation[i].loc.length<p.genes.size() || exploringItemPSO.gBestLocation.loc.length<p.genes.size() )
			//	r1=0;
			for(int j=0;j<=p.genes.size()-1;j++){
				if(p.velocity.vel.length<p.genes.size() || exploringItemPSO.pBestLocation.length<exploringItemPSO.CountOFpopulation || exploringItemPSO.pBestLocation[i].loc.length<p.genes.size() || exploringItemPSO.gBestLocation.loc.length<p.genes.size() )
					newVel[j] =p.velocity.vel[j];
				else
					newVel[j] = (w * p.velocity.vel[j]) + 
					      (r1 * C1) * (exploringItemPSO.pBestLocation[i].loc[j] - p.location.loc[j]) +
								(r2 * C2) * (exploringItemPSO.gBestLocation.loc[j] - p.location.loc[j]);
			}
			
			p.velocity.vel=new int[newVel.length];
			for(int j=0;j<=newVel.length-1;j++){
				p.velocity.vel[j]=(int)(newVel[j]);
			}
			
			//update location
			double[] newLoc = new double[exploringItemPSO.population.get(i).genes.size()];
			for(int j=0;j<=p.genes.size()-1;j++)
				newLoc[j]=p.location.loc[j]+newVel[j];
			
			p.location.loc=new int[newLoc.length];
			for(int j=0;j<=newLoc.length-1;j++)
				p.location.loc[j]=(int)(newLoc[j]);
			
			exploringItemPSO.population.set(i,p);
		}
		copy_AlllocsTogenes(exploringItemPSO);
	}
	
	public int IndexofPart_Bestfitness(ExploringItemPSO exploringItemPSO) {
		int min=0;
		if(exploringItemPSO.CTLproperty.equals("Reachability")){
			for(int i=1; i<exploringItemPSO.CountOFpopulation; i++) {
				if(exploringItemPSO.population.get(i).fitness>exploringItemPSO.population.get(min).fitness)
					min=i;
			}
		}else{
			for(int i=1; i<exploringItemPSO.CountOFpopulation; i++) {
				if(exploringItemPSO.population.get(i).fitness<exploringItemPSO.population.get(min).fitness)
					min=i;
			}
		}
		return min;
	}
	public int IndexofPart_Worstfitness(ExploringItemPSO exploringItemPSO) {
		int max=0;
		if(exploringItemPSO.CTLproperty.equals("Reachability")){
			for(int i=1; i<exploringItemPSO.CountOFpopulation; i++) {
				if(exploringItemPSO.population.get(i).fitness<exploringItemPSO.population.get(max).fitness)
					max=i;
			}
		}else{   //Deadlock
			for(int i=1; i<exploringItemPSO.CountOFpopulation; i++) {
				if(exploringItemPSO.population.get(i).fitness>exploringItemPSO.population.get(max).fitness)
					max=i;
			}
		}
		return max;
	}
	
	
	private void createInitialPopulation(ExploringItemPSO exploringItemPSO,ExploreType exploreType){
		
    	int CountOFpopulation=exploringItemPSO.CountOFpopulation;
    	int partIndex=0;
    	
    	
		while(partIndex<CountOFpopulation && exploringItemPSO.heuristicResult==null){
			if(!callFromHeuGenerator){
	    		simulator.getModel().resetGTS();
	    		exploreType=exploringItemPSO.simulator.getModel().getExploreType();
	    	}else{
	    		GTS gts=null;
				try {
					gts = new GTS(exploringItemPSO.grammer);
				} catch (FormatException e) {
					e.printStackTrace();
				}
				exploringItemPSO.gts=gts;
				exploringItemPSO.initialState=gts.startState();
	    	}
			HeuPSOExploreAction heuExploreAction=new HeuPSOExploreAction(simulator, false);
			heuExploreAction.explore(exploreType, exploringItemPSO);
			
			ExploringItemPSO.Particle particle=exploringItemPSO.population.get(partIndex);
			int len=particle.genes.size();
			particle.velocity.vel = new int[len];
			for(int i=0;i<=len-1;i++)
				particle.velocity.vel[i]=GetRandomVal(0,exploringItemPSO.maxValueInAllParticles);
			
			
			
			exploringItemPSO.population.set(partIndex,particle);
			
			
			partIndex++;
 	    }
			
	}
	private void Calculate_Mass(ExploringItemPSO exploringItemPSO){
		int indexOFpartBestFit=IndexofPart_Bestfitness(exploringItemPSO);
		int indexOFpartWorstFit=IndexofPart_Worstfitness(exploringItemPSO);
		
		double sum=0;
		int partIndex;
		
		//formula 3
		
		for(partIndex=0;partIndex<CountOFpopulation;partIndex++){
			//mass(i)=(current_fitness(i)-0.99*worst)/(best-worst);
			exploringItemPSO.population.get(partIndex).mass=(exploringItemPSO.population.get(partIndex).fitness-0.99*exploringItemPSO.population.get(indexOFpartWorstFit).fitness)/(exploringItemPSO.population.get(indexOFpartBestFit).fitness-exploringItemPSO.population.get(indexOFpartWorstFit).fitness);
			sum+=exploringItemPSO.population.get(partIndex).mass;
		}
		
		//formula 4
		
		for(partIndex=0;partIndex<CountOFpopulation;partIndex++){
			// mass(i)=mass(i)*5/sum(mass);
			exploringItemPSO.population.get(partIndex).mass=exploringItemPSO.population.get(partIndex).mass*5/sum;
		}
				
	}
	private void Force_Update(ExploringItemPSO exploringItemPSO){
		/*
		 for i=1:n
			    for j=1:dim
			        for k=1:n
			            if(current_position(k,j)~=current_position(i,j))
			                % Equation (3)
			                force(i,j)=force(i,j)+ rand()*G*mass(k)*mass(i)*(current_position(k,j)-current_position(i,j))/abs(current_position(k,j)-current_position(i,j));
			                
			            end
			        end
			    end
			end
		 */
		int n=exploringItemPSO.CountOFpopulation;
		for(int i=0;i<=n-1;i++){
			int dim=exploringItemPSO.population.get(i).genes.size();
			for(int j=1;j<=dim-1;j++)
				for(int k=0;k<=n-1;k++){
					ExploringItemPSO.Particle particleI=exploringItemPSO.population.get(i);
					ExploringItemPSO.Particle particleK=exploringItemPSO.population.get(k);
					if(j<particleI.location.loc.length && j<particleK.location.loc.length && j<particleI.force.frc.length)
						if(particleI.location.loc[j]!=particleK.location.loc[j]){
							particleI.force.frc[j]+=Math.random()*exploringItemPSO.G*particleK.mass*(particleK.location.loc[j]-particleI.location.loc[j])/Math.abs((particleK.location.loc[j]-particleI.location.loc[j]));
						}
				}
		}
			
	}
	private void Acc_Velocity_Loc_Update(ExploringItemPSO exploringItemPSO){
		// Accelations $ Velocities  UPDATE 
		/*
		  for i=1:n
		       for j=1:dim
		            if(mass(i)~=0)
		                %Equation (6)
		                acceleration(i,j)=force(i,j)/mass(i);
		            end
		       end
		   end   
		 */
		int n=exploringItemPSO.CountOFpopulation;
		for(int i=0;i<=n-1;i++){
			int dim=exploringItemPSO.population.get(i).genes.size();
			ExploringItemPSO.Particle particle=exploringItemPSO.population.get(i);
			for(int j=1;j<=dim-1;j++)
				if(j<particle.acceleration.acc.length && j<particle.force.frc.length)
					if(particle.mass!=0)
						particle.acceleration.acc[j]=(double)particle.force.frc[j]/particle.mass;
		}
		/*
		 for i=1:n
		        for j=1:dim
		            %Equation(9)
		            velocity(i,j)=rand()*velocity(i,j)+C1*rand()*acceleration(i,j) + C2*rand()*(gBest(j)-current_position(i,j));
		        end
		  end
		*/
		n=exploringItemPSO.CountOFpopulation;
		for(int i=0;i<=n-1;i++){
			int dim=exploringItemPSO.population.get(i).genes.size();
			ExploringItemPSO.Particle particle=exploringItemPSO.population.get(i);
			for(int j=1;j<=dim-1;j++)
				if(j<particle.velocity.vel.length && j<particle.acceleration.acc.length && j<exploringItemPSO.gBestLocation.loc.length && j<particle.location.loc.length)
					particle.velocity.vel[j]=(int)(Math.random()*particle.velocity.vel[j]+exploringItemPSO.C1*Math.random()*particle.acceleration.acc[j]+ exploringItemPSO.C2*Math.random()*(exploringItemPSO.gBestLocation.loc[j]-particle.location.loc[j]));
		}
		//Location Update
		// current_position = current_position + velocity 
		n=exploringItemPSO.CountOFpopulation;
		for(int i=0;i<=n-1;i++){
			ExploringItemPSO.Particle p=exploringItemPSO.population.get(i);
			
			double[] newLoc = new double[p.genes.size()];
			for(int j=0;j<=p.genes.size()-1;j++)
				if(j<p.location.loc.length && j<p.velocity.vel.length)
					newLoc[j]=p.location.loc[j]+p.velocity.vel[j];
			
			for(int j=0;j<=newLoc.length-1;j++)
				if(j<p.location.loc.length)
					p.location.loc[j]=(int)(newLoc[j]);
			exploringItemPSO.population.set(i,p);
		}
		
		copy_AlllocsTogenes(exploringItemPSO);
	}
	private void createInitialPopulation_GSA(ExploringItemPSO exploringItemPSO,ExploreType exploreType){
		
    	int CountOFpopulation=exploringItemPSO.CountOFpopulation;
    	int partIndex=0;
    	
    	
		while(partIndex<CountOFpopulation && exploringItemPSO.heuristicResult==null){
			if(!callFromHeuGenerator){
	    		simulator.getModel().resetGTS();
	    		exploreType=exploringItemPSO.simulator.getModel().getExploreType();
	    	}else{
	    		GTS gts=null;
				try {
					gts = new GTS(exploringItemPSO.grammer);
				} catch (FormatException e) {
					e.printStackTrace();
				}
				exploringItemPSO.gts=gts;
				exploringItemPSO.initialState=gts.startState();
	    	}
			HeuPSOExploreAction heuExploreAction=new HeuPSOExploreAction(simulator, false);
			heuExploreAction.explore(exploreType, exploringItemPSO);
			
			ExploringItemPSO.Particle particle=exploringItemPSO.population.get(partIndex);
			int len=particle.genes.size();
			particle.velocity.vel = new int[len];
			for(int i=0;i<=len-1;i++)
				particle.velocity.vel[i]=GetRandomVal(0,exploringItemPSO.maxValueInAllParticles);
			
			//force=zeros(n,dim);
			particle.force.frc=new int[len];
			for(int i=0;i<=len-1;i++)
				particle.force.frc[i]=0;
			
			//mass(n)=0;
			particle.mass=0;
						
			//acceleration=zeros(n,dim)
			particle.acceleration.acc=new double[len];
			for(int i=0;i<=len-1;i++)
				particle.acceleration.acc[i]=0;
			
			
			exploringItemPSO.population.set(partIndex,particle);
			partIndex++;
 	    }
		
		
	}
	private void copy_AllgenesTolocs(ExploringItemPSO exploringItemPSO){
		for(int i=0;i<=exploringItemPSO.CountOFpopulation-1;i++){
			int len=exploringItemPSO.population.get(i).genes.size();
			exploringItemPSO.population.get(i).location.loc=new int[len];
			for(int j=0;j<=exploringItemPSO.population.get(i).genes.size()-1;j++)
				 exploringItemPSO.population.get(i).location.loc[j]=exploringItemPSO.population.get(i).genes.get(j);
		}
	}
	private void copy_AlllocsTogenes(ExploringItemPSO exploringItemPSO){
		for(int i=0;i<=exploringItemPSO.CountOFpopulation-1;i++)
			for(int j=0;j<=exploringItemPSO.population.get(i).genes.size()-1;j++)
				 exploringItemPSO.population.get(i).genes.set(j,exploringItemPSO.population.get(i).location.loc[j]);
	}
	private void CalcFitness(ExploringItemPSO exploringItemPSO,ExploreType exploreType ){
		
    	int CountOFpopulation=exploringItemPSO.CountOFpopulation;
    	int partIndex=0;
    	exploringItemPSO.totalFitness=0;
 	    while(partIndex<CountOFpopulation && exploringItemPSO.heuristicResult==null){
 	    	if(!callFromHeuGenerator){
 	    		simulator.getModel().resetGTS();
 	    		exploreType=exploringItemPSO.simulator.getModel().getExploreType();
 	    		exploringItemPSO.exploreType=exploreType;
 	    	}else{
 	    		GTS gts=null;
				try {
					gts = new GTS(exploringItemPSO.grammer);
				} catch (FormatException e) {
					e.printStackTrace();
				}
 				exploringItemPSO.gts=gts;
 				exploringItemPSO.initialState=gts.startState();
 	    	}
 	    	exploringItemPSO.partIndex=partIndex;
 	    	HeuPSOExploreAction heuExploreAction=new HeuPSOExploreAction(simulator, false);
			heuExploreAction.explore(exploreType, exploringItemPSO);
			heuExploreAction=null;
 	    	
 	    	
			partIndex++;
 	    }
	}
	
	
	
    private int GetRandomVal(int min, int max)
    {
         return  (int) (min + Math.random() * (max - min));
    }
	
	private void sortPopulation(ExploringItemPSO exploringItemPSO){
		///////////////////////////////bubble sort///
		///sort based on fitness
		///if (exploringItemPSO.ModelCheckingTarget.equals("DeadLock")) then Incremental Sorting
		///if(!exploringItemPSO.ModelCheckingTarget.equals("DeadLock")) then Decremental Sorting
		///////////////
		
		if(exploringItemPSO.ModelCheckingTarget.equals("DeadLock")){
			boolean swapped = true;
			int j = 0;
			ExploringItemPSO.Particle  tmp;
			while (swapped){
				swapped = false;
				j++;
				for (int i = 0; i < exploringItemPSO.population.size() - j; i++) {
					if (exploringItemPSO.population.get(i).fitness > exploringItemPSO.population.get(i+1).fitness) {
					    tmp = exploringItemPSO.population.get(i);
					    exploringItemPSO.population.set(i, exploringItemPSO.population.get(i+1));
					    exploringItemPSO.population.set(i+1,tmp);
					    swapped = true;
					}
				}
			}

		}else{
			boolean swapped = true;
			int j = 0;
			ExploringItemPSO.Particle  tmp;
			while (swapped){
				swapped = false;
				j++;
				for (int i = 0; i < exploringItemPSO.population.size() - j; i++) {
					if (exploringItemPSO.population.get(i).fitness < exploringItemPSO.population.get(i+1).fitness) {
					    tmp = exploringItemPSO.population.get(i);
					    exploringItemPSO.population.set(i, exploringItemPSO.population.get(i+1));
					    exploringItemPSO.population.set(i+1,tmp);
					    swapped = true;
					}
				}
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
	public  double C1;
	public  double C2;
	public  double W;
	
	
	
	
	
	
	
	/**
	 * Save all paths from final state's to initial state.
	*/
	
	
	public ExploringItemPSO exploringItemPSO;
	public Boolean callFromHeuGenerator=false;
	public long Number_Explored_States;
	public long First_Found_Dead_depth; //The first found deadlock depth
	public long First_Found_Dead_Rep;  //The first deadlock is found after how many repetitions
	public long Call_Number_Fitness;  //The number of fitness function calls
	public String psoType="PSO-GSA"; 	
	 
	
	
}

		