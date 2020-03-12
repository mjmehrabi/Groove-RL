
// Java GUI packages
package groove.verify;
import javax.swing.*;

public class FPGrowth extends JFrame {

    /* ------ FIELDS ------ */

      // Data structures
	
	private static final long serialVersionUID = 1L;

	/** 2-D aray to hold input data from data file. Note that within the data
    array records are numbered from zero, thus rexcord one has index 0 etc. */
    protected short[][] dataArray = null;
   
    /** 2-D array used to renumber columns for input data in terms of
    frequency of single attributes (reordering will enhance performance
    for some ARM algorithms). */
    protected int[][] conversionArray   = null;
    /** 1-D array used to reconvert input data column numbers to their
    original numbering where the input data has been ordered to enhance
    computational efficiency. */
    protected short[] reconversionArray = null;
	/** Command line argument for number of columns. */
    protected int     numCols    = 0;
    /** Command line argument for number of rows. */
    protected int     numRows    = 0;
    /** Command line argument for % support (default = 20%). */
    protected double  support    = 20.0;
    /** Minimum support value in terms of number of rows. <P>Set when input
    data is read and the number of records is known,   */
    protected double  minSupport = 0;
    /** The number of one itemsets (singletons). */
    protected int numOneItemSets = 0;
    /** Flag to indicate whether system has data or not. */
    private boolean haveDataFlag = false;
    /** Flag to indicate whether input data has been sorted or not. */
    protected boolean isOrderedFlag = false;
    /** Flag to indicate whether input data has been sorted and pruned or
    not. */
    protected boolean isPrunedFlag = false;

    /* ------ CONSTRUCTORS ------ */
    public  FPGrowth() {

	  }
       
    /* INPUT DATA SET */

    /** Commences process of getting input data (GUI version also exists). */

    public void inputDataSetR(short[][] a,int nR,double minsupFP)
    {
    	support=minsupFP;
    	//numCols=4;
    	numRows=nR;
    	dataArray=new short[nR][];
    	
    	short[] TmpArr=new short[100];
    	int TmpArrLn;
    	for (int i=0;i<numRows;i++)
    	{
    		TmpArrLn=0;
    		for (int j=0;j<100;j++)
    			TmpArr[j]=-1;
    		for (int j=0;j<a[i].length;j++){
    			if (notMemberOf(a[i][j],TmpArr)){
    				TmpArr[TmpArrLn]=a[i][j];
    				TmpArrLn++;
    			}
    		}
    		dataArray[i] = new short[TmpArrLn];
    		for(int j=0;j<TmpArrLn;j++)
    	    	{
    		    			dataArray[i][j]=TmpArr[j];
    	    	}
    		
    	}

		//minSupport = (numRows * support)/100.0;
    	minSupport = (numRows * support);
    //  	System.out.println("Min support       = " + twoDecPlaces(minSupport) + " (records)");
    
		for (int i=0;i<dataArray.length;i++)
      		sortItemSet(dataArray[i]);
    	countNumCols();

/*   System.out.println("dataArray is");
		for (int i=0;i<nR;i++)
   	    {
   		for(int j=0;j<dataArray[i].length;j++)
   	    	{
   			  System.out.print(dataArray[i][j]);
   			  System.out.print(",");
   	    	}
  		System.out.println();
   	}*/
      	checkOrdering();
    }
    

        /* CHECK DATASET ORDERING */
    /** Checks that data set is ordered correctly.
    @return true if appropriate ordering, false otherwise. */

    protected boolean checkOrdering() {
        boolean result = true;

	// Loop through input data
	for(int index=0;index<dataArray.length;index++) {
	    if (!checkLineOrdering(index+1,dataArray[index])) {
		haveDataFlag = false;
		result=false;
		}
	    }

	// Return
	return(result);
	}

    /* CHECK LINE ORDERING */
    /** Checks whether a given line in the input data is in numeric sequence.
    @param lineNum the line number.
    @param itemSet the item set represented by the line
    @return true if OK and false otherwise. */

    protected boolean checkLineOrdering(int lineNum, short[] itemSet) {
        for (int index=0;index<itemSet.length-1;index++) {
	    if (itemSet[index] > itemSet[index+1]) {
		JOptionPane.showMessageDialog(null,"FILE FORMAT ERROR:\n" +
	       		"Attribute data in line " + lineNum +itemSet[index]+itemSet[index+1]+
			" not in numeric order");
		return(false);
		}
	    }

	// Default return
	return(true);
	}

    /* COUNT NUMBER OF COLUMNS */
    /** Counts number of columns represented by input data. */

    protected void countNumCols() {
        	int maxAttribute=0;

	// Loop through data array
        for(int index=0;index<dataArray.length;index++) {
	    int lastIndex = dataArray[index].length-1;
	    if (dataArray[index][lastIndex] > maxAttribute)
	    		maxAttribute = dataArray[index][lastIndex];
        }

	numCols        = maxAttribute;
	numOneItemSets = numCols; 	// default value only
	}

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*        REORDER DATA SET ACCORDING TO ATTRIBUTE FREQUENCY         */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* REORDER INPUT DATA: */

    /** Reorders input data according to frequency of
    single attributes. <P> Example, given the data set:
    <PRE>
    1 2 5
    1 2 3
    2 4 5
    1 2 5
    2 3 5
    </PRE>
    This would produce a countArray (ignore index 0):
    <PRE>
    +---+---+---+---+---+---+
    |   | 1 | 2 | 3 | 4 | 5 |
    +---+---+---+---+---+---+
    |   | 3 | 5 | 2 | 1 | 4 |
    +---+---+---+---+---+---+
    </PRE>
    Which sorts to:
    <PRE>
    +---+---+---+---+---+---+
    |   | 2 | 5 | 1 | 3 | 4 |
    +---+---+---+---+---+---+
    |   | 5 | 4 | 3 | 2 | 1 |
    +---+---+---+---+---+---+
    </PRE>
    Giving rise to the conversion Array of the form (no index 0):
    <PRE>
    +---+---+---+---+---+---+
    |   | 3 | 1 | 4 | 5 | 2 |
    +---+---+---+---+---+---+
    |   | 3 | 5 | 2 | 1 | 4 |
    +---+---+---+---+---+---+
    </PRE>
    Note that the second row here are the counts which no longer play a role
    in the conversion exercise. Thus to the new column number for column 1 is 
    column 3 (i.e. the first vale at index 1). The reconversion array of the 
    form:
    <PRE>
    +---+---+---+---+---+---+
    |   | 2 | 5 | 1 | 3 | 4 |
    +---+---+---+---+---+---+		
    </PRE> */
    
    public void idInputDataOrdering() {
	
	// Count singles and store in countArray;	     
        int[][] countArray = countSingles();
        
	// Bubble sort count array on support value (second index)	
	orderCountArray(countArray);
       
        // Define conversion and reconversion arrays      
	defConvertArrays(countArray);
	
	// Set sorted flag
	isOrderedFlag = true;
	}
	   
    /* COUNT SINGLES */
    
    /** Counts number of occurrences of each single attribute in the
    input data.
    @return 2-D array where first row represents column numbers
    and second row represents support counts. */
    
    protected int[][] countSingles() {
        
	// Dimension and initialize count array
	int[][] countArray = new int[numCols+1][2];
	for (int index=0;index<countArray.length;index++) {
	    countArray[index][0] = index;
	    countArray[index][1] = 0;
	    }
	    	    
	// Step through input data array counting singles and incrementing
	// appropriate element in the count array
	
	for(int rowIndex=0;rowIndex<dataArray.length;rowIndex++) {
	     if (dataArray[rowIndex] != null) {
		for (int colIndex=0;colIndex<dataArray[rowIndex].length;
					colIndex++) {
			countArray[dataArray[rowIndex][colIndex]][1]++;
           }
		}
	    }
	// Return
	return(countArray);
	}
	
    /* ORDER COUNT ARRAY */
    
    /** Bubble sorts count array produced by <TT>countSingles</TT> method
    so that array is ordered according to frequency of single items. 
    @param countArray The 2-D array returned by the <TT>countSingles</TT> 
    method. */
       
    private void orderCountArray(int[][] countArray) {
        int attribute, quantity;	
        boolean isOrdered;
        int index; 
               
        do {
	    isOrdered = true;
            index     = 1; 
            while (index < (countArray.length-1)) {
                if (countArray[index][1] >= countArray[index+1][1]) index++;
	        else {
	            isOrdered=false;
                    // Swap
		    attribute              = countArray[index][0];
		    quantity               = countArray[index][1];
	            countArray[index][0]   = countArray[index+1][0];
	            countArray[index][1]   = countArray[index+1][1];
                    countArray[index+1][0] = attribute;
	            countArray[index+1][1] = quantity;
	            // Increment index
		    index++;  
	            }
	  	}     
	    } while (isOrdered==false);
    	}
    
    /* DEFINE CONVERSION ARRAYS: */

    /** Defines conversion and reconversion arrays.
    @param countArray The 2-D array sorted by the <TT>orderCcountArray</TT>
    method.*/

    protected void defConvertArrays(int[][] countArray) {

	// Dimension arrays

	conversionArray   = new int[numCols+1][2];
    reconversionArray = new short[numCols+1];

	// Assign values

	for(int index=1;index<countArray.length;index++) {
        conversionArray[countArray[index][0]][0] = index;
        conversionArray[countArray[index][0]][1] = countArray[index][1];
	    reconversionArray[index] = (short) countArray[index][0];
	    }

	// Diagnostic ouput if desired
	//outputConversionArrays();
	}

   
  
    /* RECAST INPUT DATA AND REMOVE UNSUPPORTED SINGLE ATTRIBUTES. */

    /** Recasts the contents of the data array so that each record is
    ordered according to ColumnCounts array and excludes non-supported
    elements. <P> Proceed as follows:

    1) For each record in the data array. Create an empty new itemSet array.
    2) Place into this array any column numbers in record that are
       supported at the index contained in the conversion array.
    3) Assign new itemSet back into to data array */

    public void recastInputDataAndPruneUnsupportedAtts() {
        short[] itemSet;
	int attribute;

	// Step through data array using loop construct

        for(int rowIndex=0;rowIndex<dataArray.length;rowIndex++) {
	    // Check for empty row
	    if (dataArray[rowIndex]!= null) {
	        itemSet = null;
	        // For each element in the current record find if supported with
	        // reference to the conversion array. If so add to "itemSet".
	    	for(int colIndex=0;colIndex<dataArray[rowIndex].length;colIndex++) {
	            attribute = dataArray[rowIndex][colIndex];
		    // Check support
		    if (conversionArray[attribute][1] >= minSupport) {
		        itemSet = reallocInsert(itemSet,(short) conversionArray[attribute][0]);
		        }
		    }
	        // Return new item set to data array
	        dataArray[rowIndex] = itemSet;
	 	}
	    }

        // Set isPrunedFlag (used with GUI interface)
	isPrunedFlag=true;
	// Reset number of one item sets field
	numOneItemSets = getNumSupOneItemSets();
	}

    /* GET NUM OF SUPPORTE ONE ITEM SETS */
    /** Gets number of supported single item sets (note this is not necessarily
    the same as the number of columns/attributes in the input set).
    @return Number of supported 1-item sets */

    protected int getNumSupOneItemSets() {
        int counter = 0;

	// Step through conversion array incrementing counter for each
	// supported element found

	for (int index=1;index < conversionArray.length;index++) {
	    if (conversionArray[index][1] >= minSupport) counter++;
	    }

	// Return

	return(counter);
	}

  
    /**&&&*******&&& Reconverts given item set according to contents of reconversion array.
    @param itemSet the fgiven itemset.
    @return the reconverted itemset. */	
    
    protected short[] reconvertItemSet(short[] itemSet) {
        // If no conversion return orginal item set
	if (reconversionArray==null) return(itemSet); 
	
	// If item set null return null
	if (itemSet==null) return(null);
	
	// Define new item set
	short[] newItemSet = new short[itemSet.length];
	
	// Copy
	for(int index=0;index<newItemSet.length;index++) {
	    newItemSet[index] = reconversionArray[itemSet[index]];
	    }
	
	// Return
	return(newItemSet);    
        }

    /** Reconvert single item if appropriate. 
    @param item the given item (attribute).
    @return the reconvered item. */
    
    protected short reconvertItem(short item) {
        // If no conversion return orginal item
	if (reconversionArray==null) return(item); 
	
	// Otherwise rerturn reconvert item
	return(reconversionArray[item]);
	}
	

    /* ----------------------------------------------- */
    /*                                                 */
    /*        ITEM SET INSERT AND ADD METHODS          */
    /*                                                 */
    /* ----------------------------------------------- */

    /* REALLOC INSERT */

    /** Resizes given item set so that its length is increased by one
    and new element inserted.
    @param oldItemSet the original item set
    @param newElement the new element/attribute to be inserted
    @return the combined item set */

    protected short[] reallocInsert(short[] oldItemSet, short newElement) {

	// No old item set

	if (oldItemSet == null) {
	    short[] newItemSet = {newElement};
	    return(newItemSet);
	    }

	// Otherwise create new item set with length one greater than old
	// item set

	int oldItemSetLength = oldItemSet.length;
	short[] newItemSet = new short[oldItemSetLength+1];

	// Loop

	int index1;
	for (index1=0;index1 < oldItemSetLength;index1++) {
	    if (newElement < oldItemSet[index1]) {
		newItemSet[index1] = newElement;
		// Add rest
		for(int index2 = index1+1;index2<newItemSet.length;index2++)
				newItemSet[index2] = oldItemSet[index2-1];
		return(newItemSet);
		}
	    else newItemSet[index1] = oldItemSet[index1];
	    }

	// Add to end

	newItemSet[newItemSet.length-1] = newElement;

	// Return new item set

	return(newItemSet);
	}

    /* REALLOC 1 */

    /**&&&&&&&&&***********&&&&&&&&&&****** Resizes given item set so that its length is increased by one
    and appends new element (identical to append method)
    @param oldItemSet the original item set
    @param newElement the new element/attribute to be appended
    @return the combined item set */

    protected short[] realloc1(short[] oldItemSet, short newElement) {

	// No old item set

	if (oldItemSet == null) {
	    short[] newItemSet = {newElement};
	    return(newItemSet);
	    }

	// Otherwise create new item set with length one greater than old
	// item set

	int oldItemSetLength = oldItemSet.length;
	short[] newItemSet = new short[oldItemSetLength+1];

	// Loop

	int index;
	for (index=0;index < oldItemSetLength;index++)
		newItemSet[index] = oldItemSet[index];
	newItemSet[index] = newElement;

	// Return new item set

	return(newItemSet);
	}

    /* REALLOC 2 */

    /** Resizes given array so that its length is increased by one element
    and new element added to front
    @param oldItemSet the original item set
    @param newElement the new element/attribute to be appended
    @return the combined item set */

    protected short[] realloc2(short[] oldItemSet, short newElement) {

	// No old array

	if (oldItemSet == null) {
	    short[] newItemSet = {newElement};
	    return(newItemSet);
	    }

	// Otherwise create new array with length one greater than old array

	int oldItemSetLength = oldItemSet.length;
	short[] newItemSet = new short[oldItemSetLength+1];

	// Loop

	newItemSet[0] = newElement;
	for (int index=0;index < oldItemSetLength;index++)
		newItemSet[index+1] = oldItemSet[index];

	// Return new array

	return(newItemSet);
	}
	
    /* --------------------------------------------- */
    /*                                               */
    /*            ITEM SET DELETE METHODS            */
    /*                                               */
    /* --------------------------------------------- */

    /* REMOVE ELEMENT N */
    
    /** Removes the nth element/attribute from the given item set.
    @param oldItemSet the given item set.
    @param n the index of the element to be removed (first index is 0). 
    @return Revised item set with nth element removed. */
    
    protected short[] removeElementN(short [] oldItemSet, int n) {
        if (oldItemSet.length <= n) return(oldItemSet);
	else {
	    short[] newItemSet = new short[oldItemSet.length-1];
	    for (int index=0;index<n;index++) newItemSet[index] = 
	    				oldItemSet[index];
	    for (int index=n+1;index<oldItemSet.length;index++) 
	        			newItemSet[index-1] = oldItemSet[index];
	    return(newItemSet);
	    }
	}
	
        /* TWO DECIMAL PLACES */
    
    /** Converts given real number to real number rounded up to two decimal 
    places. 
    @param number the given number.
    @return the number to two decimal places. */ 
    
    protected double twoDecPlaces(double number) {
    	int numInt = (int) ((number+0.005)*100.0);
	number = ((double) numInt)/100.0;
	return(number);
	}
    /* --------------------------------------- */
    /*                                         */
    /*             SORT ITEM SET               */
    /*                                         */
    /* --------------------------------------- */

    /* SORT ITEM SET: Given an unordered itemSet, sort the set */

    /** Sorts an unordered item set.
    @param itemSet the given item set. */

    protected void sortItemSet(short[] itemSet) {
        short temp;
        boolean isOrdered;
        int index;

        do {
	    isOrdered = true;
            index     = 0;
            while (index < (itemSet.length-1)) {
                if (itemSet[index] <= itemSet[index+1]) index++;
	        else {
	            isOrdered=false;
                    // Swap
		    temp = itemSet[index];
	            itemSet[index] = itemSet[index+1];
                    itemSet[index+1] = temp;
	            // Increment index
		    index++;
	            }
	  	}
	    } while (isOrdered==false);
   	}  

    /* NOT MEMBER OF */
    
    /** Checks whether a particular element/attribute identified by a 
    column number is not a member of the given item set.
    @param number the attribute identifier (column number).
    @param itemSet the given item set.
    @return true if first argument is not a member of itemSet, and false 
    otherwise */
    
    protected boolean notMemberOf(short number, short[] itemSet) {
        
	// Loop through itemSet
	
	for(int index=0;index<itemSet.length;index++) {
//	    if (number < itemSet[index]) return(true);
	    if (number == itemSet[index]) return(false);
	    }
	
	// Got to the end of itemSet and found nothing, return true
	
	return(true);
	}

    
    }

