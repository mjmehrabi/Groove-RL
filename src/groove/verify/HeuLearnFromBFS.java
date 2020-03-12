package groove.verify;

import groove.explore.ExploreType;
import groove.grammar.Condition;
import groove.grammar.Grammar;
import groove.grammar.QualName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


import groove.grammar.aspect.AspectEdge;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.model.RuleModel;
import groove.grammar.rule.RuleEdge;
import groove.grammar.rule.RuleNode;
import groove.gui.Simulator;
import groove.gui.SimulatorModel;
import groove.gui.action.HeuLearnFromBFSExploreAction;
import groove.lts.GTS;
import groove.util.parse.FormatException;
/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */



public class HeuLearnFromBFS{
	public HeuLearnFromBFS(){
		exploringItems=new ExploringItem();
	}
	
	public void EnableSelectedHostGraph(){
        try {
            simulator.getModel().doEnableUniquely(ResourceKind.HOST, QualName.name(HostGraphName));
        } catch (IOException exc) {
        	System.err.println("Error during %s enabling" +ResourceKind.HOST.getDescription());
        }
        
	}
	
	public String Explore(int iterations,int maxNumberOfStates,String targetRule,int RulesCount,ArrayList<QualName> RulesName, String typeOfLearn,Grammar grammer,GrammarModel grammermodel){
		
			
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
			        				// do nothing
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
			
			
			exploringItems=new ExploringItem();
			exploringItems.callFromHeuGenerator=callFromHeuGenerator;
			exploringItems.grammer=grammer;
			exploringItems.grammermodel=grammermodel;
			ExploreType exploreType=null;
			if(!callFromHeuGenerator){
				simulator.getModel().resetGTS();  //Creates a fresh GTS and fires an update event.
				GTS gts = simulator.getModel().getGTS();
				exploringItems.initialState=gts.startState();
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
				exploringItems.gts=gts;
				exploringItems.initialState=gts.startState();
				exploringItems.exploreType=exploreType;
			}
			
			exploringItems.Number_Explored_States=0;
			exploringItems.First_Found_Dead_depth=0;
			exploringItems.typeOfLearn=typeOfLearn;
			exploringItems.CTLproperty=CTLproperty;   //deadlock || reachability ||safetyByReach || liveByCycle ||liveByDeadlock
			exploringItems.targetRule=targetRule;  //modelcheckingType
			exploringItems.Alltype=Alltype;
			exploringItems.maxNumberOfStates=maxNumberOfStates;
			exploringItems.minsup=minsup;
			exploringItems.maxDepth=maxDepth;

	
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
                			exploringItems.targetGraph_edgeList.add(re);
                			if(!exploringItems.targetGraph_nodeList.contains(re.source()))
                				exploringItems.targetGraph_nodeList.add(re.source());
                			if(!exploringItems.targetGraph_nodeList.contains(re.target()))
                				exploringItems.targetGraph_nodeList.add(re.target());
                		}
                		for(Condition cond : allcond){
                			ExploringItem.NAC nac=exploringItems.getNewNAC();
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
                			exploringItems.allNACs.add(nac);
                		}
                		break;
            		}
           	}
			
			
			//////////////////////////////////////////
			//////////////////////////////////////////
			
			////////////////////starting of Learning (Learning of Naive Bayes Classifier)
			
			
			exploringItems.targetRule=targetRule;
			exploringItems.simulator=simulator;
	    	
			
			
			////////////////////////////////////
			ArrayList<QualName> Alltype=new ArrayList<QualName>();
    		
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

           	exploringItems.Alltype=Alltype;
           	
			////////////////////////////
			
					
			
			
			
			
			exploringItems.isProgressVisible=false;;
			exploringItems.CTLproperty=CTLproperty;
			
			if(callFromHeuGenerator)
				exploringItems.isProgressVisible=false;  //!!!Mandatory!! Otherwise An Error will be occurred
			
			exploringItems.RulesCount=RulesCount;
			exploringItems.RulesName=RulesName;
			
			exploringItems.Number_Explored_States=0;
			
			HeuLearnFromBFSExploreAction heuExploreAction=new HeuLearnFromBFSExploreAction(simulator, false);
			boolean  isLearningStep=true;
			heuExploreAction.explore(exploreType, exploringItems, maxNumberOfStates, isLearningStep);
			
			exploringItems.isProgressVisible=false;
			
			if(callFromHeuGenerator)
				exploringItems.isProgressVisible=false;  //!!!Mandatory!! Otherwise An Error will be occurred
			
			
			if(!callFromHeuGenerator)
				exploringItems.gtsLearning=exploringItems.simulator.getModel().getGTS();
			else
				exploringItems.gtsLearning=exploringItems.gts;
	        
	      
			
	        ///////////////////////////
			
	        
			int Maxrepeat=iterations;
			exploringItems.repeat=1;
			while(exploringItems.repeat<=Maxrepeat && exploringItems.heuristicResult==null){
				
				if(exploringItems.repeat%10==0){
					isLearningStep=true;
				}
				else{
					isLearningStep=false;
				}
				
				
				if(exploringItems.repeat%10==1 || isLearningStep)
					exploringItems.isFirstStep=true;
				else
					exploringItems.isFirstStep=false;
				
				
				if(exploringItems.allpath_From_S0_To_Max.size()==0 && exploringItems.CTLproperty.equals("liveByCycle")){
					exploringItems.isFirstStep=true;
					isLearningStep=true;
				}
				
				if(!callFromHeuGenerator){
		    		simulator.getModel().resetGTS();
		    		exploreType=exploringItems.simulator.getModel().getExploreType();
		    		exploringItems.gts=simulator.getModel().getGTS();
		    		exploringItems.initialState=exploringItems.gts.startState();
		    	}else{
		    		
		    		GTS gts=null;
					try {
						gts = new GTS(exploringItems.grammer);
					} catch (FormatException e) {
						// do nothing
						e.printStackTrace();
					}
		    		exploringItems.gts=gts;
		    		exploringItems.initialState=gts.startState();
		    	}
				heuExploreAction=new HeuLearnFromBFSExploreAction(simulator, false);
				heuExploreAction.explore(exploreType, exploringItems, maxNumberOfStates, isLearningStep);
				heuExploreAction=null;
				exploringItems.repeat++;
			} //end of while
			
			
			Number_Explored_States=exploringItems.Number_Explored_States;
			First_Found_Dead_depth=exploringItems.First_Found_Dead_depth;
			
			
			if(!callFromHeuGenerator==true)
				if(exploringItems.heuristicResult==null)
					simulator.getModel().resetGTS();
			
			
       	    
			////////////////////////////////
			///////////////////////////////
			if(callFromHeuGenerator==true){
				if(exploringItems.heuristicResult.equals("reachability")){
					return "The property is verified."+" Target state found in depth:"+ exploringItems.First_Found_Dead_depth +" The number of explored states:"+exploringItems.Number_Explored_States +" ";
				}
				else
					return "The property is not verified.";
			}
			////////////////////////////////
			///////////////////////////////
			
						
			if(exploringItems.heuristicResult==null)
				exploringItems.heuristicResult="noreachability";
			else
				exploringItems.heuristicResult="reachability"+"_"+exploringItems.lastStateInReachability.toString();
					
			return "";
	}

			
	
	public Simulator simulator;
	public String HostGraphName;
	public String ModelCheckingType;
	public String ModelCheckingTarget;
	public String CTLproperty="";
	public long Number_Explored_States;
	public long First_Found_Dead_depth; //The first found deadlock depth
	public ArrayList<QualName> Alltype;
	public Double minsup=0.6;
	public Integer maxDepth=500;
	
	/**
	 * Save all paths from final state's to initial state.
	*/

	public ExploringItem exploringItems;
	public Boolean callFromHeuGenerator=false;
	
}

		