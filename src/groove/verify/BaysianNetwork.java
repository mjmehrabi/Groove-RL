package groove.verify;


import java.util.ArrayList;


/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */


public class BaysianNetwork {
	
	public BaysianNetwork(){
		Nodes=new ArrayList<Node>();
	}
	
	public ArrayList<Node> Nodes;
	
	public Node getNewNode(){
		return new Node();
	}
	public Nodeitem getNewNodeitem(){
		return new Nodeitem();
	}		
	public class Node{
		public Node(){
			NodeItems=new ArrayList<Nodeitem>();
			AllRulesInNode=new ArrayList<String>();
		}
		public ArrayList<Nodeitem> NodeItems;
		public ArrayList<String> AllRulesInNode;
	}
	
	public class Nodeitem{
		public Nodeitem(){
			curRulename="";
			prevRulename="";
			secondPrevRulename="";
			thirdPrevRulename="";
			fourthPrevRulename="";
			probability=0;
		}
		public String curRulename;
		public String prevRulename;
		public String secondPrevRulename;
		public String thirdPrevRulename;
		public String fourthPrevRulename;
		public double probability;
	}
	
}


