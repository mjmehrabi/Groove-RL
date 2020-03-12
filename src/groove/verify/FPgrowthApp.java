/* ---------------------------------------------------------------------- */
/* ---------------------------------------------------------------------- */
package groove.verify;

import groove.grammar.Rule;
import groove.verify.LearningItem.Item;

import java.util.ArrayList;

public class FPgrowthApp {
    
    private short[][] pathcode;
	public ArrayList<Rule> allRulesFP;
	public ArrayList<String> allRulesNamesFP;
	public ArrayList<String>  allpathFP;
	public String ExportedpatternFP="";
    public double minsupFP;
    // ---------------- CONSTRUCTORS ---------------------

	public FPgrowthApp(LearningItem learnitem,double minsup,ArrayList<Rule> allR, ArrayList<String> allRN){
		minsupFP=minsup;
		allRulesFP=allR;;
		allRulesNamesFP=allRN;
		allpathFP=learnitem.allpath_From_Max_To_s0;
        pathcode=new short[allpathFP.size()][];
		for(int i=0;i<=allpathFP.size()-1;i++)
		{
		    String[] t=allpathFP.get(i).split(",");
		    pathcode[i]=new short[t.length];
		    for (int j=0;j<=t.length-1;j++){
		        pathcode[i][j]=RuleCode(t[j],allRulesNamesFP);
		    }
		}

	}
    
	public short RuleCode(String RuleName,ArrayList<String> RuleNames){
    	short RCode=0;
    	for (short i=1;i<=RuleNames.size();i++){
    		if(RuleName.equals(RuleNames.get(i-1)))
    			RCode=i;
    	}
    	return(RCode);
    }

    
    // ------------------ METHODS ------------------------

	public void RunFPgrowthAPP(LearningItem learnitem){

		FPtree newFPtree = new FPtree();
		newFPtree.inputDataSetR(pathcode,pathcode.length,minsupFP);
		newFPtree.idInputDataOrdering();
		newFPtree.recastInputDataAndPruneUnsupportedAtts(); 
		newFPtree.setNumOneItemSets();
		newFPtree.createFPtree();
    	newFPtree.startMining();
	    newFPtree.retFrequentSets();
	    newFPtree.convertFpq();
/*
	    for(int i=0;i<newFPtree.bfp.length;i++){
		 for (int j=0;j<newFPtree.bfp[i].length;j++){
			System.out.print(newFPtree.bfp[i][j]);
		  }
		 System.out.println();
		 System.out.println(newFPtree.bsup[i]);
	    }
*/

	    ////
		learnitem.Exportedpattern="";
		int maxi=0;
        int maxL=newFPtree.bfp[0].length;
		for(int i=1;i<newFPtree.bfp.length;i++){
			if(newFPtree.bfp[i].length>maxL){
				maxL=newFPtree.bfp[i].length;
			    maxi=i;
			}
		}
		
		int max=maxi;
		for(int i=maxi+1;i<=newFPtree.bsup.length-1;i++){
			if ((newFPtree.bsup[i]> newFPtree.bsup[max]) && (newFPtree.bfp[i].length==maxL))
				max=i;
		}
/*		learnitem.Exportedpattern=allRulesNamesFP.get(newFPtree.bfp[max][0]);
		for(int i=1;i<newFPtree.bfp[max].length;i++)
			learnitem.Exportedpattern=learnitem.Exportedpattern+","+allRulesNamesFP.get(newFPtree.bfp[max][i]);
*/
		learnitem.Exportedpattern=allRulesNamesFP.get(newFPtree.bfp[max][0]-1);
		for(int i=1;i<newFPtree.bfp[max].length;i++)
			learnitem.Exportedpattern=learnitem.Exportedpattern+","+allRulesNamesFP.get(newFPtree.bfp[max][i]-1);

	}
    	
}
    