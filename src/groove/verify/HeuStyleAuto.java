package groove.verify;


import groove.grammar.QualName;
import groove.grammar.Rule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import groove.explore.Exploration;
import groove.explore.ExploreType;
import groove.grammar.aspect.AspectEdge;
import groove.grammar.aspect.AspectGraph;
import groove.grammar.aspect.AspectLabel;
import groove.grammar.aspect.AspectNode;


import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.model.RuleModel;

import groove.grammar.type.TypeLabel;

import groove.gui.Simulator;
import groove.gui.SimulatorModel;
import groove.gui.action.ExploreAction;
import groove.gui.action.HeuGAExploreAction;
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

public class HeuStyleAuto{
	public HeuStyleAuto(){
		ALearningItems=new ArrayList<LearningItem>();
		
	}
	
	
	public void EnableSelectedHostGraph(){
	
        try {
            simulator.getModel().doEnableUniquely(ResourceKind.HOST,QualName.name(HostGraphName));
        } catch (IOException exc) {
        	System.err.println("Error during %s enabling" +ResourceKind.HOST.getDescription());
        }
        
	}
	
	public Boolean Gen_Explore_MakeKnowlege_SmallerModel(int percentOfSmallerModel){
		

		long startTime = System.currentTimeMillis();
		
		this.percentOfSmallerModel=percentOfSmallerModel;
		
		Set<? extends AspectEdge> Host_edgeSet_Large=simulator.getModel().getGrammar().getGraphResource(ResourceKind.HOST, QualName.name(HostGraphName)).getSource().edgeSet();
		Set<? extends AspectNode> Host_NodeSet_Large=simulator.getModel().getGrammar().getGraphResource(ResourceKind.HOST, QualName.name(HostGraphName)).getSource().nodeSet();
		
		exploringAuto=new ExploringAuto();
		exploringAuto.maxRepetitionOfGenSmallerModel=10; 
		
		
		////////////////////////////////
		
		ArrayList<QualName> Alltype=new ArrayList<QualName>();
		
		GrammarModel grammermodel=simulator.getModel().getGrammar();
		Set<QualName> sname= grammermodel.getNames(ResourceKind.RULE);
		
		
		Iterator<QualName> it=sname.iterator();
		while(it.hasNext()){
			QualName ts=it.next();
			RuleModel rulemodel=grammermodel.getRuleModel(ts);
			if(rulemodel.isEnabled()){
				Set<? extends AspectEdge> edgeSet=rulemodel.getSource().edgeSet();
			
				boolean flag=false;
				for(AspectEdge ae:edgeSet ){
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
				
				if(!flag)
					Alltype.add(ts);
				}
		}

		exploringAuto.Alltype=Alltype;

		////////////////////////////

		
		
		
		Analyse_Large_Model(exploringAuto, Host_edgeSet_Large, Host_NodeSet_Large);
		
		GenerateSmallerModel(exploringAuto, percentOfSmallerModel, Host_edgeSet_Large, Host_NodeSet_Large);
		
		
		
		if(!exploringAuto.isReachToCorrectSmaller){
			Set<QualName> names=simulator.getModel().getSelectSet(ResourceKind.HOST);
			names.clear();
			names.add(QualName.name("Gsmaller"));
			try{
				simulator.getModel().doDelete(ResourceKind.HOST,names);
			}catch (IOException e) {
	        }
			
			return false;
		}
		
		
		
		
		
		if(ModelCheckingType.equals("RefuteLivenessByCycle")){
			
			//String t="";
			//for(int i=1;i<=exploringAuto.pathLeadCycle.size()-1;i++)
			//	t+=","+exploringAuto.pathLeadCycle.get(i).rule.getFullName();
			//"null,getmemo_goReady,getCPU,goWaitIO,getIO,initProcess,initProcess,initProcess,initProcess,initProcess,releaseIO,getCPU,goWaitIO,getIO"
			//"getmemo_goReady,getCPU,goWaitIO,getIO,initProcess,releaseIO,getCPU,goWaitIO,getIO"
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
			
			/////extend a learnitem.ExportedpatternNorepeat 
			int p=learnitem.ExportedpatternNorepeat.size()-1;
			for(int i=1;i<=10;i++){
				for(int j=learnitem.startIndexofCycle;j<=p;j++){
					String s=learnitem.ExportedpatternNorepeat.get(j);
					learnitem.ExportedpatternNorepeat.add(s);
				}
			}
			
			ALearningItems.add(learnitem);
			
			reportTime_Gen_Exhau_Explo_Small= System.currentTimeMillis() - startTime;
			
		}else{
			Explore();
			reportTime_Gen_Exhau_Explo_Small= System.currentTimeMillis() - startTime;
			startTime = System.currentTimeMillis();
			MakeKnowlegeBase();
			reportTime_Data_Mining= System.currentTimeMillis() - startTime;
		}

  
		
		Set<QualName> names=simulator.getModel().getSelectSet(ResourceKind.HOST);
		names.clear();
		names.add(QualName.name("Gsmaller"));
		try{
			simulator.getModel().doDelete(ResourceKind.HOST,names);
		}catch (IOException e) {
        }
        
		
		return true;
		
        
	}
	
	public String start(int percentOfSmallerModel){
		
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
			 heuExploreAction.explore(exploreType, ALearningItems, ModelCheckingType, ModelCheckingTarget, isFirstStep);
			 heuristicResult=ALearningItems.get(0).heuristicResult;
	     	
			 ALearningItems.get(0).isProgressVisible=false;
			 
			 if(heuristicResult==null){
				 int Maxrepeat=10;
				 int repeat=1;
				 while(repeat<=Maxrepeat && ALearningItems.get(0).heuristicResult==null){
					
					 simulator.getModel().resetGTS();
					 gts=simulator.getModel().getGTS();
	
					 exploreType=simulatorModel.getExploreType();	
					 heuExploreAction=new HeuStyleExploreAction(simulator, false);
					 isFirstStep=false;
					 heuExploreAction.explore(exploreType, ALearningItems, ModelCheckingType, ModelCheckingTarget, isFirstStep);
					 repeat++;
				 }
			 }
			 
			
			 
			if(ALearningItems.get(0).heuristicResult==null)
				simulator.getModel().resetGTS();
			 
			 heuristicResult=ALearningItems.get(0).heuristicResult;
			 
			 
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
		
	   
	}
	
	private void Analyse_Large_Model(ExploringAuto exploringAuto,Set<? extends AspectEdge> Host_edgeSet_Large,Set<? extends AspectNode> Host_NodeSet_Large){
		divide_host_into_clusters(exploringAuto, Host_edgeSet_Large, Host_NodeSet_Large);
		merge_clusrers_same_type(exploringAuto);
		fill_allTypesOfCluster(exploringAuto,Host_edgeSet_Large);
	}
	
	public void GenerateSmallerModel(ExploringAuto exploringAuto,int percentOfSmallerModel,Set<? extends AspectEdge> Host_edgeSet_Large,Set<? extends AspectNode> Host_NodeSet_Large){
		
			exploringAuto.isReachToCorrectSmaller=false;
			int repGen=1;
			while(!exploringAuto.isReachToCorrectSmaller && repGen<=exploringAuto.maxRepetitionOfGenSmallerModel){
				Set<QualName> names=simulator.getModel().getSelectSet(ResourceKind.HOST);
				names.clear();
				names.add(QualName.name("Gsmaller"));
				try{
					simulator.getModel().doDelete(ResourceKind.HOST,names);
				}catch (IOException e) {
	            }
	            
				final AspectGraph newGraph =
		                AspectGraph.emptyGraph("Gsmaller", ResourceKind.HOST.getGraphRole());
				try{
				simulator.getModel().doAddGraph(ResourceKind.HOST, newGraph, false);
				}catch (IOException e) {
				}
			
				
				
				
	
				for(int i=0;i<=exploringAuto.allCluster.size()-1;i++){
					ExploringAuto.Cluster cluster=exploringAuto.allCluster.get(i);
					cut_of_cluster_insert_newGraph(newGraph,cluster,percentOfSmallerModel,Host_edgeSet_Large);
				}
				
				try{
					simulator.getModel().doEnableUniquely(ResourceKind.HOST, QualName.name("Gsmaller"));
				}catch (IOException e) {
		        }
		        
				
								
				simulator.getModel().resetGTS();  //Creates a fresh GTS and fires an update event.
				GTS gts = simulator.getModel().getGTS();
				SimulatorModel simulatorModel = simulator.getModel();
				ExploreType exploreType=simulatorModel.getExploreType();
				GraphState state = simulatorModel.getState();
				HeuStyleExploreAction heuExploreAction=new HeuStyleExploreAction(simulator, false);
				heuExploreAction.exploreSmallModel(simulatorModel.getState(), exploreType);
								
				if(ModelCheckingType.equals("DeadLock")){
					Collection<GraphState> resultstates=gts.getFinalStates();
					//gts.getResultStates()
					
					if(gts.nodeCount()>=4 && !resultstates.isEmpty())
						exploringAuto.isReachToCorrectSmaller=true;
					else 
						exploringAuto.isReachToCorrectSmaller=false;
				}else if(ModelCheckingType.equals("Reachability")){
					exploringAuto.isReachToCorrectSmaller=false;
					Collection<GraphState> resultstates=FindTargetStates(gts.edgeSet());
					if(!resultstates.isEmpty()){					
						exploringAuto.isReachToCorrectSmaller=true;
						break;
					}
					
				}else if(ModelCheckingType.equals("RefuteLivenessByCycle")){
					Set<? extends GraphState> nodeset=gts.nodeSet();
					Set<? extends GraphTransition> edgeset=gts.edgeSet();
					
					exploringAuto.isFindpathLeadCycle=false;
					exploringAuto.checkedStates.add(state);
					ArrayList<StateRule> pathLeadCycle=new ArrayList<StateRule>();
					
					dfs_pathCycle(null, null,state,nodeset,edgeset);
					
					exploringAuto.isReachToCorrectSmaller=exploringAuto.isFindpathLeadCycle;
					
				}
				
				
				if(!exploringAuto.isReachToCorrectSmaller){
					exploringAuto=new ExploringAuto();
					Analyse_Large_Model(exploringAuto, Host_edgeSet_Large, Host_NodeSet_Large);
				}
				
				
				
				repGen++;
			}   //////end of while
			
			
			
	}
	
	public void dfs_pathCycle(GraphState prestate,Rule rule,GraphState state,Set<? extends GraphState> nodeset,Set<? extends GraphTransition> edgeset){
		
		//// prestate-----rule--->state///////////////
		GrammarModel grammermodel=simulator.getModel().getGrammar();
		ExploringAuto.PartialPath prepartialPath=null;
		/////Detect a cycle
		if(rule!=null && !exploringAuto.Alltype.contains(rule.toString())){
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
	
		/////is this state satisfy the ModelCheckingTarget property?
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
				    dfs_pathCycle(state, nextrule, nextState, nodeset, edgeset);
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
	
	int clusterIndex=-1;
	int itemIndex=-1;
	int clusterSourceIndex=-1;
	int itemSourceIndex=-1;
	int clusterTargetIndex=-1;
	int itemTargetIndex=-1;
	int percentOfSmallerModel=0;
	ExploringAuto exploringAuto;

	
	public void fill_allTypesOfCluster(ExploringAuto exploringAuto,Set<? extends AspectEdge> Host_edgeSet){
		for(int i=0;i<=exploringAuto.allCluster.size()-1;i++){
			ExploringAuto.Cluster cluster=exploringAuto.allCluster.get(i);
			if(cluster.isAllTypeEquall){
				if(!cluster.allTypesOfCluster.contains(cluster.typeOfCluster))
					cluster.allTypesOfCluster.add(cluster.typeOfCluster);
			}else{
				for(int j=0;j<=cluster.allItem.size()-1;j++){
					ExploringAuto.Item item=cluster.allItem.get(j);
					TypeLabel tl=findTypeLabel(Host_edgeSet,item.Asnode);
					if(!cluster.allTypesOfCluster.contains(tl))
						cluster.allTypesOfCluster.add(tl);
				}
			}
			cluster.CopyOfallTypesOfCluster=(ArrayList<TypeLabel>)cluster.allTypesOfCluster.clone();
			exploringAuto.allCluster.set(i,cluster);
		}
	}
	
	private Boolean is_should_stop(ExploringAuto.Cluster cluster){
		Boolean flag=false;
		int max_number_to_insert=(cluster.allItem.size()*percentOfSmallerModel)/100+1;
		if(max_number_to_insert>cluster.allItem.size())
			max_number_to_insert=cluster.allItem.size();
		Set<? extends AspectNode> Host_NodeSet=simulator.getModel().getGrammar().getGraphResource(ResourceKind.HOST,QualName.name("Gsmaller")).getSource().nodeSet();
		Set<? extends AspectEdge> Host_EdgeSet=simulator.getModel().getGrammar().getGraphResource(ResourceKind.HOST, QualName.name("Gsmaller")).getSource().edgeSet();
		if(cluster.allTypesOfCluster.size()==0){
			if(Host_NodeSet.size()<max_number_to_insert)
				flag=false;
			else
				flag=true;
		}else{
			flag=false;
		}
		
		///////Check again///////////////////////////
		if(flag && cluster.CopyOfallTypesOfCluster.size()==1){
			TypeLabel tl=cluster.CopyOfallTypesOfCluster.get(0);
			int count=0;
			for(AspectEdge ae:Host_EdgeSet){
				String s=ae.getTypeLabel().toString();
				if(ae.isLoop() && ae.getTypeLabel().toString().contains(tl.text()))
					count++;
			}
			
			if(count<max_number_to_insert)
				flag=false;
		}
		////////////////////////////////////
		
				
		return flag;
	}
	
	public void cut_of_cluster_insert_newGraph(final AspectGraph newGraph,ExploringAuto.Cluster cluster,int percentOfSmallerModel,Set<? extends AspectEdge> Host_edgeSet){
	
		
		int max_number_to_insert=(cluster.allItem.size()*percentOfSmallerModel)/100+1;
		if(max_number_to_insert>cluster.allItem.size())
			max_number_to_insert=cluster.allItem.size();
		
		for(int i=0;i<=cluster.allItem.size()-1;i++){
			ExploringAuto.Item item=cluster.allItem.get(i);
			item.AsnodeIsvisited=false;
			for(int j=0;j<=item.allAsEdgeOut.size()-1;j++){
				item.allAsEdgeOutISvisited.add(false);
			}
			for(int j=0;j<=item.allAsEdgeIn.size()-1;j++){
				item.allAsEdgeInISvisited.add(false);
			}
			cluster.allItem.set(i, item);
		}
		
		int startIndexItem=-1;
		for(int i=0;i<=cluster.allItem.size()-1;i++){
			ExploringAuto.Item item=cluster.allItem.get(i);
			if(item.allAsEdgeIn.size()==0){
				startIndexItem=i;
				break;
			}
		}
		//to find an item with the most item.allAsEdgeIn.size
		if(startIndexItem>=0){
			for(int i=startIndexItem+1;i<=cluster.allItem.size()-1;i++){
				ExploringAuto.Item maxitem=cluster.allItem.get(startIndexItem);
				ExploringAuto.Item item=cluster.allItem.get(i);
				if(item.allAsEdgeIn.size()==0 && item.allAsEdgeIn.size()>maxitem.allAsEdgeIn.size() ){
					startIndexItem=i;
				}
			}
		}
		
			
		if(startIndexItem>=0){
			dfs_visit(startIndexItem,newGraph,cluster,Host_edgeSet);
		}
		while(!is_should_stop(cluster)){
			int r=GetRandomVal(0, cluster.allItem.size());
			if(r>=0){
				dfs_visit(r,newGraph,cluster,Host_edgeSet);
			}
		}	
		
		////////////for connecting the unconnected items
		
		for(int i=0;i<=cluster.allItem.size()-1;i++){
			ExploringAuto.Item startItem=cluster.allItem.get(i);
			if(startItem.AsnodeIsvisited==true){
				for(int j=0;j<=startItem.allAsEdgeOut.size()-1;j++){
					if(startItem.allAsEdgeOutISvisited.get(j)==false){
						AspectEdge ae=startItem.allAsEdgeOut.get(j);
						int newitemIndex=find_asnode(cluster,ae.label(),ae.target());
						if(newitemIndex>=0){
							ExploringAuto.Item tempItem=cluster.allItem.get(newitemIndex);
							tempItem.Asnode.allFixed=false;
							startItem.Asnode.allFixed=false;
							AspectEdge aeNew=new AspectEdge(startItem.Asnode,ae.label(),tempItem.Asnode);
							newGraph.addEdge(aeNew);
							startItem.allAsEdgeOutISvisited.set(j, true);
							break;
						}
					}
				}
			}
		}
	}
	public Boolean isEquall_twoItems(ExploringAuto.Item item1,ExploringAuto.Item item2){
		Boolean flag=true;
		for(int i=0;i<=item1.allAsEdgeSelf.size()-1;i++){
			AspectEdge ae1=item1.allAsEdgeSelf.get(i);
			String aeStr1=ae1.getTypeLabel().toString();
			flag=false;
			for(int j=0;j<=item2.allAsEdgeSelf.size()-1;j++){
				AspectEdge ae2=item2.allAsEdgeSelf.get(i);
				String aeStr2=ae2.getTypeLabel().toString();
				if(aeStr1.equals(aeStr2)){
					flag=true;
					break;
				}
			}
			if(flag==false)
				return false;
		}
		return flag;
	}
	public Boolean is_in_cluster_unvisited(ExploringAuto.Cluster cluster){
		Boolean flag=false;
		for(int i=0;i<=cluster.allItem.size()-1;i++){
			ExploringAuto.Item item=cluster.allItem.get(i);
			if(item.AsnodeIsvisited==false){
				return true;
			}
		}
		return flag;
	}
	public void dfs_visit(int itemIndex,final AspectGraph newGraph,ExploringAuto.Cluster cluster,Set<? extends AspectEdge> Host_edgeSet){
		
		ExploringAuto.Item item=cluster.allItem.get(itemIndex);
		if(cluster.allItem.get(itemIndex).AsnodeIsvisited==false){
			AspectNode ans=item.Asnode;
			newGraph.addNode(ans);
			for(int j=0;j<=item.allAsEdgeSelf.size()-1;j++){
				AspectEdge ae=item.allAsEdgeSelf.get(j);
				newGraph.addEdge(ae);
			}
			
			ans.allFixed=true;
			
			item.AsnodeIsvisited=true;
			cluster.allTypesOfCluster.remove(findTypeLabel(Host_edgeSet, ans));
		}
		if(is_should_stop(cluster))
			return;
		
		
		
		if(Math.random()<=0.5){    ///////////item.allAsEdgeOut//////////////
			////	
			for(int j=0;j<=item.allAsEdgeOut.size()-1;j++){
				if(item.allAsEdgeOutISvisited.get(j)==false){
					AspectEdge ae=item.allAsEdgeOut.get(j);
					if(cluster.allTypesOfCluster.contains(findTypeLabel(Host_edgeSet, ae.target()))){
						int newitemIndex=set_asnode_visited(newGraph, cluster, ae.source(),ae.target(),true);
					    newGraph.addEdge(ae);
						item.allAsEdgeOutISvisited.set(j, true);
						cluster.allTypesOfCluster.remove(findTypeLabel(Host_edgeSet, ae.target()));
						if(is_should_stop(cluster))
							return;
						dfs_visit(newitemIndex, newGraph, cluster,Host_edgeSet);
						if(is_should_stop(cluster))
							return;
						break;
					}
				}
			}
			for(int j=0;j<=item.allAsEdgeOut.size()-1;j++){
				if(item.allAsEdgeOutISvisited.get(j)==false){
					AspectEdge ae=item.allAsEdgeOut.get(j);
					int newitemIndex=set_asnode_visited(newGraph, cluster,ae.source(), ae.target(),true);
					newGraph.addEdge(ae);
					item.allAsEdgeOutISvisited.set(j, true);
					if(is_should_stop(cluster))
						return;
					dfs_visit(newitemIndex, newGraph, cluster,Host_edgeSet);
					if(is_should_stop(cluster))
						return;
				}
			}
						
			/////
			for(int j=0;j<=item.allAsEdgeIn.size()-1;j++){
				if(item.allAsEdgeInISvisited.get(j)==false){
					AspectEdge ae=item.allAsEdgeIn.get(j);
					if(cluster.allTypesOfCluster.contains(findTypeLabel(Host_edgeSet, ae.source()))){
						int newitemIndex=set_asnode_visited(newGraph, cluster, ae.source(),ae.target(),false);
						newGraph.addEdge(ae);
						item.allAsEdgeInISvisited.set(j, true);
						cluster.allTypesOfCluster.remove(findTypeLabel(Host_edgeSet, ae.source()));
						if(is_should_stop(cluster))
							return;
						dfs_visit(newitemIndex, newGraph, cluster,Host_edgeSet);
						if(is_should_stop(cluster))
							return;
						break;
					}
				}
			}
			for(int j=0;j<=item.allAsEdgeIn.size()-1;j++){
				if(item.allAsEdgeInISvisited.get(j)==false){
					AspectEdge ae=item.allAsEdgeIn.get(j);
					int newitemIndex=set_asnode_visited(newGraph, cluster, ae.source(),ae.target(),false);
					newGraph.addEdge(ae);
					item.allAsEdgeInISvisited.set(j, true);
					if(is_should_stop(cluster))
						return;
					dfs_visit(newitemIndex, newGraph, cluster,Host_edgeSet);
					if(is_should_stop(cluster))
						return;
				}
			}	
			////
		}else { /////////////////////item.allAsEdgeIn//////////////////
			/////
			for(int j=0;j<=item.allAsEdgeIn.size()-1;j++){
				if(item.allAsEdgeInISvisited.get(j)==false){
					AspectEdge ae=item.allAsEdgeIn.get(j);
					if(cluster.allTypesOfCluster.contains(findTypeLabel(Host_edgeSet, ae.source()))){
						int newitemIndex=set_asnode_visited(newGraph, cluster, ae.source(),ae.target(),false);
						newGraph.addEdge(ae);
						item.allAsEdgeInISvisited.set(j, true);
						cluster.allTypesOfCluster.remove(findTypeLabel(Host_edgeSet, ae.source()));
						if(is_should_stop(cluster))
							return;
						dfs_visit(newitemIndex, newGraph, cluster,Host_edgeSet);
						if(is_should_stop(cluster))
							return;
						break;
					}
				}
			}
			for(int j=0;j<=item.allAsEdgeIn.size()-1;j++){
				if(item.allAsEdgeInISvisited.get(j)==false){
					AspectEdge ae=item.allAsEdgeIn.get(j);
					int newitemIndex=set_asnode_visited(newGraph, cluster, ae.source(),ae.target(),false);
					newGraph.addEdge(ae);
					item.allAsEdgeInISvisited.set(j, true);
					if(is_should_stop(cluster))
						return;
					dfs_visit(newitemIndex, newGraph, cluster,Host_edgeSet);
					if(is_should_stop(cluster))
						return;
				}
			}	
			////
			for(int j=0;j<=item.allAsEdgeOut.size()-1;j++){
				if(item.allAsEdgeOutISvisited.get(j)==false){
					AspectEdge ae=item.allAsEdgeOut.get(j);
					if(cluster.allTypesOfCluster.contains(findTypeLabel(Host_edgeSet, ae.target()))){
						int newitemIndex=set_asnode_visited(newGraph, cluster, ae.source(),ae.target(),true);
						newGraph.addEdge(ae);
						item.allAsEdgeOutISvisited.set(j, true);
						cluster.allTypesOfCluster.remove(findTypeLabel(Host_edgeSet, ae.target()));
						if(is_should_stop(cluster))
							return;
						dfs_visit(newitemIndex, newGraph, cluster,Host_edgeSet);
						if(is_should_stop(cluster))
							return;
						break;
					}
				}
			}
			for(int j=0;j<=item.allAsEdgeOut.size()-1;j++){
				if(item.allAsEdgeOutISvisited.get(j)==false){
					AspectEdge ae=item.allAsEdgeOut.get(j);
					int newitemIndex=set_asnode_visited(newGraph, cluster, ae.source(),ae.target(),true);
					newGraph.addEdge(ae);
					item.allAsEdgeOutISvisited.set(j, true);
					if(is_should_stop(cluster))
						return;
					dfs_visit(newitemIndex, newGraph, cluster,Host_edgeSet);
					if(is_should_stop(cluster))
						return;
				}
			}
			/////////
		}///////////end if
		
	}
	
	public int set_asnode_visited(final AspectGraph newGraph,ExploringAuto.Cluster cluster,AspectNode ansSource,AspectNode ansTarget,Boolean isOutEdge){
		int newitemIndex=-1;
		if(isOutEdge==true){         /////OutEdge
			for(int i=0;i<=cluster.allItem.size()-1;i++){
				ExploringAuto.Item item=cluster.allItem.get(i);
				if(item.Asnode.equals(ansTarget)){
					newGraph.addNode(ansTarget);
					for(int j=0;j<=item.allAsEdgeSelf.size()-1;j++){
						AspectEdge ae=item.allAsEdgeSelf.get(j);
						newGraph.addEdge(ae);
					}
					
					item.AsnodeIsvisited=true;
					item.AsnodeIsvisited=true;
					cluster.allItem.set(i, item);
					newitemIndex=i;
					for(int j=0;j<=item.allAsEdgeIn.size()-1;j++){
						if(item.allAsEdgeIn.get(j).source().equals(ansSource)){
							item.allAsEdgeInISvisited.set(j,true);
							break;
						}
					}
				}
			}
		}else{      /////InEdge
			for(int i=0;i<=cluster.allItem.size()-1;i++){
				ExploringAuto.Item item=cluster.allItem.get(i);
				if(item.Asnode.equals(ansSource)){
					newGraph.addNode(ansSource);
					for(int j=0;j<=item.allAsEdgeSelf.size()-1;j++){
						AspectEdge ae=item.allAsEdgeSelf.get(j);
						newGraph.addEdge(ae);
					}
					
					item.AsnodeIsvisited=true;
					item.AsnodeIsvisited=true;
					cluster.allItem.set(i, item);
					newitemIndex=i;
					for(int j=0;j<=item.allAsEdgeOut.size()-1;j++){
						if(item.allAsEdgeOut.get(j).target().equals(ansTarget)){
							item.allAsEdgeOutISvisited.set(j,true);
							break;
						}
					}
				}
			}
		
		}
		return newitemIndex;
	}
	public int find_asnode(ExploringAuto.Cluster cluster,AspectLabel label,AspectNode ans){
		
		ExploringAuto.Item fitem=null;
		for(int i=0;i<=cluster.allItem.size()-1;i++){
			ExploringAuto.Item item=cluster.allItem.get(i);
			if(item.Asnode.equals(ans)){
				fitem=item;
				break;
			}
		}
		
		int newitemIndex=-1;
		for(int i=0;i<=cluster.allItem.size()-1;i++){
			ExploringAuto.Item item=cluster.allItem.get(i);
			if(item.AsnodeIsvisited==true && isEquall_twoItems(item, fitem)){
				for(int j=0;j<=item.allAsEdgeIn.size()-1;j++){
					if(item.allAsEdgeInISvisited.get(j)==false && item.allAsEdgeIn.get(j).label().equals(label)){
						return i;
					}
				}
			}
		}
		return newitemIndex;
	}
	
	public void divide_host_into_clusters(ExploringAuto exploringAuto,Set<? extends AspectEdge> Host_edgeSet,Set<? extends AspectNode> Host_NodeSet){
		
		for(AspectNode an:Host_NodeSet){
			ExploringAuto.Cluster cluster=null;
			ExploringAuto.Item item=null;
			cluster=exploringAuto.getNewCluster();
			item=exploringAuto.getNewItem();
			item.Asnode=an;
			cluster.allItem.add(item);
			cluster.isAllTypeEquall=true;
			cluster.typeOfCluster=null;
			exploringAuto.allCluster.add(cluster);
		}
		for(AspectEdge ae:Host_edgeSet){
			AspectNode anSource=ae.source();   //source==   ----->  target
			AspectNode anTarget=ae.target();
			
			if(ae.isLoop()){
				ExploringAuto.Cluster cluster=null;
				ExploringAuto.Item item=null;
				find_cluster_contain_item(exploringAuto, anSource);
				cluster=exploringAuto.allCluster.get(clusterIndex);
				item=cluster.allItem.get(itemIndex);
				item.allAsEdgeSelf.add(ae);
				cluster.allItem.set(itemIndex, item);
				cluster.typeOfCluster=findTypeLabel(Host_edgeSet, anSource);
				exploringAuto.allCluster.set(clusterIndex,cluster);
			}else{
				ExploringAuto.Cluster clusterSource=null;
				ExploringAuto.Item itemSource=null;
				find_cluster_contain_item(exploringAuto, anSource);
				clusterSourceIndex=clusterIndex;
				itemSourceIndex=itemIndex;
				clusterSource=exploringAuto.allCluster.get(clusterSourceIndex);
				itemSource=clusterSource.allItem.get(itemSourceIndex);
				itemSource.allAsEdgeOut.add(ae);
				clusterSource.allItem.set(itemSourceIndex, itemSource);
				clusterSource.typeOfCluster=findTypeLabel(Host_edgeSet, anSource);
				
				
				exploringAuto.allCluster.set(clusterSourceIndex,clusterSource);
			
				ExploringAuto.Cluster clusterTarget=null;
				ExploringAuto.Item itemTarget=null;
				find_cluster_contain_item(exploringAuto, anTarget);
				clusterTargetIndex=clusterIndex;
				itemTargetIndex=itemIndex;
				clusterTarget=exploringAuto.allCluster.get(clusterTargetIndex);
				itemTarget=clusterTarget.allItem.get(itemTargetIndex);
				itemTarget.allAsEdgeIn.add(ae);
				clusterTarget.allItem.set(itemTargetIndex, itemTarget);
				clusterTarget.typeOfCluster=findTypeLabel(Host_edgeSet, anTarget);
								
				exploringAuto.allCluster.set(clusterTargetIndex,clusterTarget);
				
				if(clusterSourceIndex!=clusterTargetIndex){
					//Merge two clusters
					clusterTarget=exploringAuto.allCluster.get(clusterTargetIndex);
					clusterSource=exploringAuto.allCluster.get(clusterSourceIndex);
					for(int i=0;i<=clusterTarget.allItem.size()-1;i++){
						itemTarget=clusterTarget.allItem.get(i);
						clusterSource.allItem.add(itemTarget);	
					}
					if(clusterSource.typeOfCluster!=null && clusterTarget.typeOfCluster!=null ){
						if (!(clusterSource.isAllTypeEquall && clusterTarget.isAllTypeEquall && clusterSource.typeOfCluster.equals(clusterTarget.typeOfCluster))){
							clusterSource.isAllTypeEquall=false;
						}
					}
					exploringAuto.allCluster.set(clusterSourceIndex,clusterSource);
					exploringAuto.allCluster.remove(clusterTargetIndex);
				}else{
					if(!findTypeLabel(Host_edgeSet,anSource).equals(findTypeLabel(Host_edgeSet,anTarget))){
						clusterSource=exploringAuto.allCluster.get(clusterSourceIndex);
						clusterSource.isAllTypeEquall=false;
						exploringAuto.allCluster.set(clusterSourceIndex,clusterSource);
					}
				}
			}
		}

	}
	
	public void merge_clusrers_same_type(ExploringAuto exploringAuto){
		int i=0;
		while(i<=exploringAuto.allCluster.size()-1){
			ExploringAuto.Cluster icluster=exploringAuto.allCluster.get(i);
			if(icluster.isAllTypeEquall){
				int j=i+1;
				while(j<=exploringAuto.allCluster.size()-1){
					ExploringAuto.Cluster jcluster=exploringAuto.allCluster.get(j);
					if(jcluster.isAllTypeEquall && icluster.typeOfCluster.equals(jcluster.typeOfCluster)){
						for(int k=0;k<=jcluster.allItem.size()-1;k++){
							ExploringAuto.Item item=jcluster.allItem.get(k);
							icluster.allItem.add(item);	
						}
						exploringAuto.allCluster.set(i,icluster);
						exploringAuto.allCluster.remove(j);
						j--;
					}
					j++;
				}
			}
			i++;
		}
		
	}
	
	public TypeLabel findTypeLabel(Set<? extends AspectEdge> Host_edgeSet,AspectNode an){
		TypeLabel tl=null;
		for(AspectEdge ae:Host_edgeSet){
			AspectNode anSource=ae.source();   //source==   ----->  target
			AspectNode anTarget=ae.target();
			if(ae.isLoop() && anSource.equals(an) ){
				if(ae.getTypeLabel().toString().contains("type")){
					tl=ae.getTypeLabel();
					return tl;
				}
			}
		}
		
		return tl;
	}
	
	public void find_cluster_contain_item(ExploringAuto exploringAuto,AspectNode an){
		clusterIndex=-1;
		itemIndex=-1;
		for(int i=0;(i<=exploringAuto.allCluster.size()-1) && (clusterIndex==-1);i++){
			ExploringAuto.Cluster cluster=exploringAuto.allCluster.get(i);
			for(int j=0;(j<=cluster.allItem.size()-1) && (clusterIndex==-1)  ;j++){
				ExploringAuto.Item item=cluster.allItem.get(j);
				if(item.Asnode.equals(an)){
					clusterIndex=i;
					itemIndex=j;
					return;
				}
			}
				
		}
		
	}
	
	
	public void Explore(){
		ALearningItems.clear();
		LearningItem learnitem=new LearningItem();
		ALearningItems.add(learnitem);
		ALearningItems.get(0).isProgressVisible=true;
		
		
		simulator.getModel().resetGTS();  //Creates a fresh GTS and fires an update event.
		gts = simulator.getModel().getGTS();
		SimulatorModel simulatorModel = simulator.getModel();
		ExploreType exploreType=simulatorModel.getExploreType();	
		ExploreAction exploreAction=new ExploreAction(simulator, false);
		exploreAction.explore(exploreType);
		ALearningItems.get(0).isProgressVisible=false;
	}
	GTS gts;
	
	public void MakeKnowlegeBase(){
		//try{

		ALearningItems.clear();
		//GTS gts = simulator.getModel().getGTS();
		Set<? extends GraphState> nodeset=gts.nodeSet();
		Set<? extends GraphTransition> edgeset=gts.edgeSet();
		
		
		GrammarModel grammermodel=simulator.getModel().getGrammar();
		
		initialState=gts.startState();
		initialStateName=gts.startState().toString();
		
		Collection<GraphState> resultstates;
		
		
		
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
			
			FindFreqPatt_Apriori(learnitem,0.1);
			
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
			
			
		
			ALearningItems.set(x, learnitem);
		
		}
		
				
		
		
		////////
			
		
		///////
		
		
		//}
		//catch (FormatException e) {
	     //      System.err.println(e.getMessage());
	    //}
		
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
		
		for(int k=1;learnitem.CK_Items.size()>0;k++){
		
			//////////////make C_k+1
			learnitem.Ctemp_Items.clear();
			for(int i=0;i<=learnitem.CK_Items.size()-1;i++)
				for(int j=0;j<=learnitem.C1_Items.size()-1;j++){
					LearningItem.Item item=learnitem.getNewItem();
					item.rules=learnitem.CK_Items.get(i).rules+","+learnitem.C1_Items.get(j).rules;
					learnitem.Ctemp_Items.add(item);
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
	
	private int GetRandomVal(int min, int max)
    {
         return  (int) (min + Math.random() * (max - min));
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
	

	
    
 	
 	
	
	
	public Simulator simulator;
	public String HostGraphName;
	public String ModelCheckingType;
	public String ModelCheckingTarget;
	
	
	private GraphState initialState;
	private String initialStateName;
	
	/**
	 * Save all paths from final state's to initial state.
	*/
	public ArrayList<LearningItem> ALearningItems;
	
    public long reportTime_Data_Mining;  //Executing of the FindFreqPatt algorithm for data mining
    public long reportTime_Gen_Exhau_Explo_Small;   //Generating and Exhaustive exploring of the samller model
    public long Number_Explored_States;   //Number of explored states
    
  

	 
}

		