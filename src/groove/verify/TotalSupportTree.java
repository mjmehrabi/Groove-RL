package groove.verify;
//import TtreeNode;
import java.util.ArrayList;
/* Structure:

      |
      +-- TotalSupportTree	 */ 


/* Java packages */

/** Methods concerned with the generation, processing and manipulation of 
T-tree data storage structures used to hold the total support counts for large 
itemsets. */

public class TotalSupportTree extends FPGrowth {

    /* ------ FIELDS ------ */
	
    /**
	 * 
	 */
	public class Fqp{
    	String Str="";
    	double sup=0;
    }
  public ArrayList<Fqp> FPFqp=new ArrayList<Fqp>();
  short[][] bfp;
  double[] bsup;
   //public ArrayList<String> FPStr=new ArrayList<String>();
   //public double[] FPSup=new double[1000];
   public double lastsup=0;

   private static final long serialVersionUID = 1L;

	// Data structures    
    /** The reference to start of t-tree. */
    protected TtreeNode[] startTtreeRef;

    // Diagnostics 

    /** The number of updates required to generate the T-tree. */
    protected long numUpdates   = 0l;

    /* ------ CONSTRUCTORS ------ */

    /** Processes command line arguments. */
    
    public TotalSupportTree() {
	super();
	}

    /* ------ METHODS ------ */

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                           ADD TO T-TREE                          */
    /*                                                                  */
    /* ---------------------------------------------------------------- */
	
    /* ADD TO T-TREE */
    
    /** Commences process of adding an itemset (with its support value) to a
    T-tree when using a T-tree either as a storage mechanism, or when adding to 
    an existing T-tree. 
    @param itemSet The given itemset. Listed in numeric order (not reverse
    numeric order!).
    @param support The support value associated with the given itemset. */
    
    public void addToTtree(short[] itemSet, int support) {
        // Determine index of last elemnt in itemSet.
	int endIndex = itemSet.length-1;
	
	// Add itemSet to T-tree.
        startTtreeRef = addToTtree(startTtreeRef,numOneItemSets+1,
			endIndex,itemSet,support);
	}
		
    /* ADD TO T-TREE */
    
    /** Inserts a node into a T-tree. <P> Recursive procedure.
    @param linkRef the reference to the current array in Ttree.
    @param size the size of the current array in T-tree.
    @param endIndex the index of the last element/attribute in the itemset, 
    which is also used as a level counter.	
    @param itemSet the given itemset.
    @param support the support value associated with the given itemset. 
    @return the reference to the revised sub-branch of t-tree. */
    
    protected TtreeNode[] addToTtree(TtreeNode[] linkRef, int size, int endIndex,
    				short[] itemSet, int support) {
	// If no array describing current level in the T-tree or T-tree
	// sub-branch create one with "null" nodes.	
	if (linkRef == null) {
	    linkRef = new TtreeNode[size];
	    for(int index=1;index<linkRef.length;index++) 
			linkRef[index] = null;
	    }
	
	// If null node at index of array describing current level in T-tree 
	// (T-tree sub-branch) create a T-tree node describing the current 
	// itemset sofar.
	int currentAttribute = itemSet[endIndex]; 
	if (linkRef[currentAttribute] == null)
	    		linkRef[currentAttribute] = new TtreeNode();
	
	// If at right level add support 	
	if (endIndex == 0) {
	    linkRef[currentAttribute].support =
	    			linkRef[currentAttribute].support + support;
	    return(linkRef);
	    }
	    
	// Otherwise proceed down branch and return	
	linkRef[currentAttribute].childRef = 
			addToTtree(linkRef[currentAttribute].childRef,
				currentAttribute,endIndex-1,itemSet,support);
	// Return
	return(linkRef);
	}

    /*---------------------------------------------------------------------- */
    /*                                                                       */
    /*                        T-TREE SEARCH METHODS                          */
    /*                                                                       */
    /*---------------------------------------------------------------------- */

    /* GET SUPPORT FOT ITEM SET IN T-TREE */

    /** Commences process for finding the support value for the given item set
    in the T-tree (which is know to exist in the T-tree). <P> Used when
    generating Association Rules (ARs). Note that itemsets are stored in
    reverse order in the T-tree therefore the given itemset must be processed
    in reverse.
    @param itemSet the given itemset.
    @return returns the support value (0 if not found). */

    protected int getSupportForItemSetInTtree(short[] itemSet) {
	int endInd = itemSet.length-1;

    	// Last element of itemset in Ttree (Note: Ttree itemsets stored in
	// reverse)
  	if (startTtreeRef[itemSet[endInd]] != null) {
	    // If "current index" is 0, then this is the last element (i.e the
	    // input is a 1 itemset)  and therefore item set found
	    if (endInd == 0) return(startTtreeRef[itemSet[0]].support);
	    // Otherwise continue down branch
	    else {
	    	TtreeNode[] tempRef = startTtreeRef[itemSet[endInd]].childRef;
	        if (tempRef != null) return(getSupForIsetInTtree2(itemSet,
							   endInd-1,tempRef));
	    	// No further branch therefore rerurn 0
		else return(0);
		}
	    }
	// Item set not in Ttree thererfore return 0
    	else return(0);
	}

    /** Returns the support value for the given itemset if found in the T-tree
    and 0 otherwise. <P> Operates recursively.
    @param itemSet the given itemset.
    @param index the current index in the given itemset.
    @param linRef the reference to the current T-tree level.
    @return returns the support value (0 if not found). */

    private int getSupForIsetInTtree2(short[] itemSet, int index,
    							TtreeNode[] linkRef) {
        // Element at "index" in item set exists in Ttree
  	if (linkRef[itemSet[index]] != null) {
  	    // If "current index" is 0, then this is the last element of the
	    // item set and therefore item set found
	    if (index == 0) return(linkRef[itemSet[0]].support);
	    // Otherwise continue provided there is a child branch to follow
	    else if (linkRef[itemSet[index]].childRef != null)
	    		          return(getSupForIsetInTtree2(itemSet,index-1,
	    		                    linkRef[itemSet[index]].childRef));
	    else return(0);
	    }	
	// Item set not in Ttree therefore return 0
	else return(0);    
    	}
			
    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                              UTILITY METHODS                           */
    /*                                                                        */
    /*----------------------------------------------------------------------- */
    
    /* SET NUMBER ONE ITEM SETS */
    
    /** Sets the number of one item sets field (<TT>numOneItemSets</TT> to 
    the number of supported one item sets. */
    
    public void setNumOneItemSets() {
        numOneItemSets=getNumSupOneItemSets();
	}

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                              OUTPUT METHODS                            */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* ---------------- */
    /*   OUTPUT T-TRRE  */
    /* ---------------- */
    
    /** Commences process of outputting T-tree structure contents to screen. */	
    public void outputTtree() {
	int number = 1;
	
	// Loop
	
	for (short index=1; index < startTtreeRef.length; index++) {
	    if (startTtreeRef[index] !=null) {
	        String itemSetSofar = 
		                    new Short(reconvertItem(index)).toString();
	        System.out.print("[" + number + "] {" + itemSetSofar);
	        System.out.println("} = " + startTtreeRef[index].support);
	        outputTtree(new Integer(number).toString(),itemSetSofar,
			                        startTtreeRef[index].childRef);
		number++;
		}
	    }   
	}
	
    /** Continue process of outputting T-tree. <P> Operates in a recursive 
    manner.
    @param number the ID number of a particular node.
    @param itemSetSofar the label for a T-tree node as generated sofar.
    @param linkRef the reference to the current array level in the T-tree. */
    
    private void outputTtree(String number, String itemSetSofar,
    				TtreeNode[] linkRef) {
	// Set output local variables.
	int num=1;
	number = number + ".";
	itemSetSofar = itemSetSofar + " ";
	
	// Check for empty branch/sub-branch.
	if (linkRef == null) return;
	
	// Loop through current level of branch/sub-branch.
	for (short index=1;index<linkRef.length;index++) {
	    if (linkRef[index] != null) {
	        String newItemSet = itemSetSofar + (reconvertItem(index));
	        System.out.print("[" + number + num + "] {" + newItemSet);
	        System.out.println("} = " + linkRef[index].support);
	        outputTtree(number + num,newItemSet,linkRef[index].childRef); 
	        num++;
		}
	    }    
	}

    /* ----------------------- */
    /*   OUTPUT FREQUENT SETS  */
    /* ----------------------- */
    /** Commences the process of outputting the frequent sets contained in
    the T-tree. */

    public void outputFrequentSets() {
	int number = 1;

	System.out.println("FREQUENT (LARGE) ITEM SETS:\n" +
	                    	"---------------------------");
	System.out.println("Format: [N] {I} = S, where N is a sequential " +
		"number, I is the item set and S the support.");

	// Loop

	for (short index=1; index <= numOneItemSets; index++) {
	    if (startTtreeRef[index] !=null) {
	        if (startTtreeRef[index].support >= minSupport) {
	            String itemSetSofar = 
		                   new Short(reconvertItem(index)).toString();
	            System.out.println("[" + number + "] {" + itemSetSofar + 
		    		       "} = " + startTtreeRef[index].support);
	            number = outputFrequentSets(number+1,itemSetSofar,
		    			 index,startTtreeRef[index].childRef);
		    }
		}
	    }

	// End

	System.out.println("\n");
	}

    /** Outputs T-tree frequent sets. <P> Operates in a recursive manner.
    @param number the number of frequent sets so far.
    @param itemSetSofar the label for a T-treenode as generated sofar.
    @param size the length/size of the current array level in the T-tree.
    @param linkRef the reference to the current array level in the T-tree.
    @return the incremented (possibly) number the number of frequent sets so
    far. */

    private int outputFrequentSets(int number, String itemSetSofar, int size,
    							TtreeNode[] linkRef) {

	// No more nodes

	if (linkRef == null) return(number);

	// Otherwise process

	itemSetSofar = itemSetSofar + " ";
	for (short index=1; index < size; index++) {
	    if (linkRef[index] != null) {
	        if (linkRef[index].support >= minSupport) {
	            String newItemSet = itemSetSofar + (reconvertItem(index));
		    System.out.println("[" + number + "] {" + newItemSet +
		                             "} = " + linkRef[index].support);
	            number = outputFrequentSets(number + 1,newItemSet,index,
		    			             linkRef[index].childRef);
	            }
		}
	    }

	// Return

	return(number);
	}

    /** Commences the process of outputting the frequent sets contained in
    the T-tree. */

    public void retFrequentSets() {
    	int number = 1;

    	// Loop
    	for (short index=1; index <= numOneItemSets; index++) {
    	    if (startTtreeRef[index] !=null) {
    	        if (startTtreeRef[index].support >= minSupport) {
    	            String itemSetSofar = 
    		                   new Short(reconvertItem(index)).toString();
    			    lastsup=startTtreeRef[index].support;
    	            number = retFrequentSets(number+1,itemSetSofar,
    		    			 index,startTtreeRef[index].childRef);
    		    }
    		}
    	    }

    	}

        /** Outputs T-tree frequent sets. <P> Operates in a recursive manner.
        @param number the number of frequent sets so far.
        @param itemSetSofar the label for a T-treenode as generated sofar.
        @param size the length/size of the current array level in the T-tree.
        @param linkRef the reference to the current array level in the T-tree.
        @return the incremented (possibly) number the number of frequent sets so
        far. */

        private int retFrequentSets(int number, String itemSetSofar, int size,
        							TtreeNode[] linkRef) {

        	if (linkRef == null){
//    		String[] s=itemSetSofar.split(",");
       		Fqp FPFqptmp=new Fqp();
       		FPFqptmp.Str=itemSetSofar;		
       		FPFqptmp.sup =lastsup;
       		FPFqp.add(FPFqptmp);

    	//	FPStr.add(itemSetSofar);
    	//	FPSup[FPStr.size()-1]=lastsup;
    		return(number);
    	}

    	// Otherwise process

    	itemSetSofar = itemSetSofar + " ";
    	for (short index=1; index < size; index++) {
    	    if (linkRef[index] != null) {
    	        if (linkRef[index].support >= minSupport) {
    	            String newItemSet = itemSetSofar +","+(reconvertItem(index));
    		    lastsup=linkRef[index].support;
    	        number = retFrequentSets(number + 1,newItemSet,index,
    		    			             linkRef[index].childRef);
    	            }
    		}
    	    }

    	return(number);
    	}
    	
   
        
        public void convertFpq(){
        	bfp=new short[FPFqp.size()][];
        	bsup=new double[FPFqp.size()];

        	for(int i=0;i<FPFqp.size();i++){
        		bfp[i]=new short[FPFqp.get(i).Str.split(",").length];
        		int j=0;
        		for (String s:FPFqp.get(i).Str.split(",")){
        		     bfp[i][j++]=Short.parseShort(s.trim());
        		}
        		bsup[i]=FPFqp.get(i).sup;
        		
        	}

        }
        /* ------------------------------ */
    /*   OUTPUT NUMBER FREQUENT SETS  */
    /* ------------------------------ */
    /** Commences the process of counting and outputing number of supported
    nodes in the T-tree.<P> A supported set is assumed to be a non null node in
    the T-tree. */

   
        public void outputNumFreqSets() {
	
	// If empty tree (i.e. no supported sets) do nothing
	if (startTtreeRef== null) System.out.println("Number of frequent " +
					"sets = 0");
	// Otherwise count and output
	else System.out.println("Number of frequent sets = " + 
					countNumFreqSets());
	}
    
    /* COUNT NUMBER OF FRQUENT SETS */
    /** Commences process of counting the number of frequent (large/supported
    sets contained in the T-tree. */
    
    protected int countNumFreqSets() {
        // If empty tree return 0
	if (startTtreeRef ==  null) return(0);

	// Otherwise loop through T-tree starting with top level
	int num=0;
	for (int index=1; index <= numOneItemSets; index++) {
	    // Check for null valued top level Ttree node.
	    if (startTtreeRef[index] !=null) {
	        if (startTtreeRef[index].support >= minSupport) 
			num = countNumFreqSets(index,
	    				startTtreeRef[index].childRef,num+1);
		}
	    }   
	
	// Return
	return(num);
	}
	
    /** Counts the number of supported nodes in a sub branch of the T-tree.
    @param size the length/size of the current array level in the T-tree.
    @param linkRef the reference to the current array level in the T-tree.
    @param num the number of frequent sets sofar. */

    protected int countNumFreqSets(int size, TtreeNode[] linkRef, int num) {
	
	if (linkRef == null) return(num);
	
	for (int index=1; index < size; index++) {
	    if (linkRef[index] != null) {
	        if (linkRef[index].support >= minSupport) 
	            			num = countNumFreqSets(index,
					linkRef[index].childRef,num+1);
		}
	    }
	
	// Return
	
	return(num);
	}
    
		
    
}
  