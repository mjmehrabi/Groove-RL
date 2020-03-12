package groove.verify;



import java.io.IOException;
import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Set;




/**
 * FIN algorithm.
 * 
 * This implementation was obtained by converting the original C++ code of FIN
 * provided by ZHIHONG DENG, to Java.
 * 
 * The code is copyright by Zhihong Deng.
 * 
 * @author R.javadpour
 */
public class FIN {

	

	// number of itemsets found
	int outputCount = 0;

	// object to write the output file
	

	public int[][] bf;
	public int bf_cursor;
	public int bf_size;
	public int bf_col;
	public int bf_currentSize;

	public int numOfFItem; // Number of items
	public int minSupport; // minimum support
	public Item[] item; // list of items sorted by support

	// public FILE out;
	public int[] result; // the current itemset
	public int resultLen = 0; // the size of the current itemset
	public int resultCount = 0;
	public int nlLenSum = 0; // node list length of the current itemset

	// Tree stuff
	public PPCTreeNode ppcRoot;
	public NodeListTreeNode nlRoot;
	public int[] itemsetCount;

	public int[] nlistBegin;
	public int nlistCol;
	public int[] nlistLen;
	public int firstNlistBegin;
	public int PPCNodeCount;
	public int[] SupportDict;

	public int[] sameItems;
	public int nlNodeCount;
	///add for store max length item
 	public ArrayList<String> path;
 	public int maxlength;

	/**
	 * Comparator to sort items by decreasing order of frequency
	 */
	static Comparator<Item> comp = new Comparator<Item>() {
		public int compare(Item a, Item b) {
			return ((Item) b).num - ((Item) a).num;
		}
	};

	private int numOfTrans;

	/**
	 * Run the algorithm
	 * 
	 * @param learnitem
	 *            the input file path
	 * @param minsup
	 *            the minsup threshold

	 */
	public void runAlgorithm(LearningItem learnitem, double minsup){
			

		ppcRoot = new PPCTreeNode();
		nlRoot = new NodeListTreeNode();
		nlNodeCount = 0;

		
		// create object for writing the output file
		

		// record the start time
		//startTimestamp = System.currentTimeMillis();

		bf_size = 1000000;
		bf = new int[100000][];
		bf_currentSize = bf_size * 10;
		bf[0] = new int[bf_currentSize];

		bf_cursor = 0;
		bf_col = 0;

		// ==========================
		// Read Dataset
		maxlength = 0;
		path = new ArrayList<String>();
		ArrayList<String>  allpath =learnitem.allpath_From_Max_To_s0;

		getData(allpath, minsup);

		resultLen = 0;
		result = new int[numOfFItem];
        allpath = shrink(allpath);
		// Build tree
		buildTree(allpath);

		nlRoot.label =  numOfFItem ;
		nlRoot.firstChild = null;
		nlRoot.next = null; 

		// Initialize tree
		initializeTree();
		sameItems = new int[numOfFItem];

		int from_cursor = bf_cursor;
		int from_col = bf_col;
		int from_size = bf_currentSize;

		// Recursively traverse the tree
		NodeListTreeNode curNode = nlRoot.firstChild;
		NodeListTreeNode next = null;
		while (curNode != null) {
			next = curNode.next;
			// call the recursive "traverse" method
			traverse(curNode, nlRoot, 1, 0);
			for (int c = bf_col; c > from_col; c--) {
				bf[c] = null;
			}
			bf_col = from_col;
			bf_cursor = from_cursor;
			bf_currentSize = from_size;
			curNode = next;
		}
	

		

		// record the end time
		learnitem.Exportedpattern = path.get(0);
		//endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * shrink rules
	 */
	ArrayList<String> shrink(ArrayList<String> allpath){
		Set<String> hs = new HashSet<>();
		ArrayList<String> allpathtemp = new ArrayList<String>() ;
		for(int ii=0;ii<allpath.size();ii++){
			// if the line is a comment, is empty or is a
			// kind of metadata
			

			// split the line into items
			String[] lineSplited = allpath.get(ii).split(",");

			// for each item in the transaction
			hs.clear();
			for (String itemString : lineSplited) {
		    	hs.add(itemString);
			}
			String str = ""; 
			for (String itemString : hs) {
		    	str = str + itemString+",";
			}
			allpathtemp.add(str);
			
		}
		
		return allpathtemp;
	}

	/**
	 * Build the tree
	 * 
	 * @param filename
	 *            the input filename
	 * @throws IOException
	 *             if an exception while reading/writting to file
	 *///throws IOException
	void buildTree(ArrayList<String> allpath)  {

		PPCNodeCount = 0;
		ppcRoot.label = -1;

		// READ THE FILE
		//BufferedReader reader = new BufferedReader(new FileReader(filename));
		//String line;

		// we will use a buffer to store each transaction that is read.
		Item[] transaction = new Item[1000];

		// for each line (transaction) until the end of the file
		//while (((line = reader.readLine()) != null))
		for(int ii=0;ii<allpath.size();ii++){
			// if the line is a comment, is empty or is a
			// kind of metadata
			

			// split the line into items
			String[] lineSplited = allpath.get(ii).split(",");

			// for each item in the transaction
			int tLen = 0; // tLen
			for (String itemString : lineSplited) {
				// get the item
				String itemX = itemString;

				// add each item from the transaction except infrequent item
				for (int j = 0; j < numOfFItem; j++) {
					// if the item appears in the list of frequent items, we add
					// it
					if (itemX.equals(item[j].index)) {
						transaction[tLen] = new Item();
						transaction[tLen].index = itemX; // the item
						transaction[tLen].num = 0 - j;
						tLen++;
						break;
					}
				}
			}

			// sort the transaction
			Arrays.sort(transaction, 0, tLen, comp);

			// Print the transaction
			// for(int j=0; j < tLen; j++){
			// System.out.print(" " + transaction[j].index + " ");
			// }
			// System.out.println();

			int curPos = 0;
			PPCTreeNode curRoot = (ppcRoot);
			PPCTreeNode rightSibling = null;
			while (curPos != tLen) {
				PPCTreeNode child = curRoot.firstChild;
				while (child != null) {
					if (child.label == 0 - transaction[curPos].num) {
						curPos++;
						child.count++;
						curRoot = child;
						break;
					}
					if (child.rightSibling == null) {
						rightSibling = child;
						child = null;
						break;
					}
					child = child.rightSibling;
				}
				if (child == null)
					break;
			}
			for (int j = curPos; j < tLen; j++) {
				PPCTreeNode ppcNode = new PPCTreeNode();
				ppcNode.label = 0 - transaction[j].num;
				if (rightSibling != null) {
					rightSibling.rightSibling = ppcNode;
					rightSibling = null;
				} else {
					curRoot.firstChild = ppcNode;
				}
				ppcNode.rightSibling = null;
				ppcNode.firstChild = null;
				ppcNode.father = curRoot;
				ppcNode.count = 1;
				curRoot = ppcNode;
				PPCNodeCount++;
			}
		}
		// close the input file
		//reader.close();

		PPCTreeNode root = ppcRoot.firstChild;
		int pre = 0;
		itemsetCount = new int[(numOfFItem - 1) * numOfFItem / 2];
		nlistBegin = new int[(numOfFItem - 1) * numOfFItem / 2];
		nlistLen = new int[(numOfFItem - 1) * numOfFItem / 2];
		SupportDict = new int[PPCNodeCount + 1];
		while (root != null) {
			root.foreIndex = pre;
			SupportDict[pre] = root.count;
			pre++;
			PPCTreeNode temp = root.father;
			while (temp.label != -1) {
				itemsetCount[root.label * (root.label - 1) / 2 + temp.label] += root.count;
				nlistLen[root.label * (root.label - 1) / 2 + temp.label]++;
				temp = temp.father;
			}
			if (root.firstChild != null) {
				root = root.firstChild;
			} else {
				if (root.rightSibling != null) {
					root = root.rightSibling;
				} else {
					root = root.father;
					while (root != null) {
						if (root.rightSibling != null) {
							root = root.rightSibling;
							break;
						}
						root = root.father;
					}
				}
			}
		}

		// build 2-itemset nlist
		int sum = 0;
		for (int i = 0; i < (numOfFItem - 1) * numOfFItem / 2; i++) {
			if (itemsetCount[i] >= minSupport) {
				nlistBegin[i] = sum;
				sum += nlistLen[i];
			}
		}
		if (bf_cursor + sum > bf_currentSize * 0.85) {
			bf_col++;
			bf_cursor = 0;
			bf_currentSize = sum + 1000;
			bf[bf_col] = new int[bf_currentSize];
		}
		nlistCol = bf_col;
		firstNlistBegin = bf_cursor;
		root = ppcRoot.firstChild;
		bf_cursor += sum;
		while (root != null) {
			PPCTreeNode temp = root.father;
			while (temp.label != -1) {
				if (itemsetCount[root.label * (root.label - 1) / 2 + temp.label] >= minSupport) {
					int cursor = nlistBegin[root.label * (root.label - 1) / 2
							+ temp.label]
							+ firstNlistBegin;
					bf[nlistCol][cursor] = root.foreIndex;
					nlistBegin[root.label * (root.label - 1) / 2 + temp.label] += 1;
				}
				temp = temp.father;
			}
			if (root.firstChild != null) {
				root = root.firstChild;
			} else {
				if (root.rightSibling != null) {
					root = root.rightSibling;
				} else {
					root = root.father;
					while (root != null) {
						if (root.rightSibling != null) {
							root = root.rightSibling;
							break;
						}
						root = root.father;
					}
				}
			}
		}
		for (int i = 0; i < numOfFItem * (numOfFItem - 1) / 2; i++) {
			if (itemsetCount[i] >= minSupport) {
				nlistBegin[i] = nlistBegin[i] - nlistLen[i];
			}
		}

	}

	/**
	 * Initialize the tree
	 */
	void initializeTree() {

		NodeListTreeNode lastChild = null;
		for (int t = numOfFItem - 1; t >= 0; t--) {
			NodeListTreeNode nlNode = new NodeListTreeNode();
			nlNode.label = t;
			nlNode.support = 0;
			nlNode.NLStartinBf = bf_cursor;
			nlNode.NLLength = 0;
			nlNode.NLCol = bf_col;
			nlNode.firstChild = null;
			nlNode.next = null;
			nlNode.support = item[t].num;
			if (nlRoot.firstChild == null) {
				nlRoot.firstChild = nlNode;
				lastChild = nlNode;
			} else {
				lastChild.next = nlNode;
				lastChild = nlNode;
			}
		}
	}

	/**
	 * Read the input file to find the frequent items
	 * 
	 * @param filename
	 *            input file name
	 * @param minSupport
	 * @throws IOException
	 */
	void getData(ArrayList<String> allpath, double minSupport)  {
		numOfTrans = 0;
		
		
		//String[] s=allpath.get(0).split(",");
		// (1) Scan the database and count the support of each item.
		// The support of items is stored in map where
		// key = item value = support count
		Map<String, Integer> mapItemCount = new HashMap<String, Integer>();
		// scan the database
		//BufferedReader reader = new BufferedReader(new FileReader(filename));
		//String line;
		// for each line (transaction) until the end of the file
		//while (((line = reader.readLine()) != null)) {
			// if the line is a comment, is empty or is a
			// kind of metadata
			//if (line.isEmpty() == true || line.charAt(0) == '#'
			//		|| line.charAt(0) == '%' || line.charAt(0) == '@') {
			//	continue;
			//}

			//numOfTrans++;
		   numOfTrans = allpath.size();
		   for(int i = 0;i<allpath.size();i++){
			   
			// split the line into items
			String[] lineSplited = allpath.get(i).split(",");
			// for each item in the transaction
			for (String itemString : lineSplited) {
				// increase the support count of the item by 1
				String item = itemString;
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}

		}
		// close the input file
		//reader.close();

		this.minSupport = (int)Math.ceil(minSupport * numOfTrans);

		numOfFItem = mapItemCount.size();

		Item[] tempItems = new Item[numOfFItem];
		int i = 0;
		for (Entry<String, Integer> entry : mapItemCount.entrySet()) {
			if (entry.getValue() >= minSupport) {
				tempItems[i] = new Item();
				tempItems[i].index = entry.getKey();
				tempItems[i].num = entry.getValue();
				i++;
			}
		}

		item = new Item[i];
		System.arraycopy(tempItems, 0, item, 0, i);

		numOfFItem = item.length;

		Arrays.sort(item, comp);
	}

	NodeListTreeNode iskItemSetFreq(NodeListTreeNode ni, NodeListTreeNode nj,
			int level, NodeListTreeNode lastChild, IntegerByRef sameCountRef) {

		if (bf_cursor + ni.NLLength > bf_currentSize) {
			bf_col++;
			bf_cursor = 0;
			bf_currentSize = bf_size > ni.NLLength * 1000 ? bf_size
					: ni.NLLength * 1000;
			bf[bf_col] = new int[bf_currentSize];
		}

		NodeListTreeNode nlNode = new NodeListTreeNode();
		nlNode.support = 0;
		nlNode.NLStartinBf = bf_cursor;
		nlNode.NLCol = bf_col;
		nlNode.NLLength = 0;

		int cursor_i = ni.NLStartinBf;
		int cursor_j = nj.NLStartinBf;
		int col_i = ni.NLCol;
		int col_j = nj.NLCol;

		while (cursor_i < ni.NLStartinBf + ni.NLLength
				&& cursor_j < nj.NLStartinBf + nj.NLLength) {
			if (bf[col_i][cursor_i] == bf[col_j][cursor_j]) {
				bf[bf_col][bf_cursor++] = bf[col_j][cursor_j];
				nlNode.NLLength++;
				nlNode.support += SupportDict[bf[col_i][cursor_i]];
				cursor_i += 1;
				cursor_j += 1;
			} else if (bf[col_i][cursor_i] < bf[col_j][cursor_j]) {
				cursor_i += 1;
			} else {
				cursor_j += 1;
			}
		}
		if (nlNode.support >= minSupport) {
			if (ni.support == nlNode.support) {
				sameItems[sameCountRef.count++] = nj.label;
			} else {
				nlNode.label = nj.label;
				nlNode.firstChild = null;
				nlNode.next = null;
				if (ni.firstChild == null) {
					ni.firstChild = nlNode;
					lastChild = nlNode;
				} else {
					lastChild.next = nlNode;
					lastChild = nlNode;
				}
			}
			return lastChild;
		} else {
			bf_cursor = nlNode.NLStartinBf;
		}
		return lastChild;
	}

	/**
	 * Recursively traverse the tree to find frequent itemsets
	 * @param curNode
	 * @param curRoot
	 * @param level
	 * @param sameCount
	 * @throws IOException if error while writing itemsets to file
	 */
	public void traverse(NodeListTreeNode curNode, NodeListTreeNode curRoot,
			int level, int sameCount)  {

		

//		System.out.println("==== traverse(): " + curNode.label + " " + level
//				+ " " + sameCount);
		NodeListTreeNode sibling = curNode.next;
		NodeListTreeNode lastChild = null;
		while (sibling != null) {
			if ((level == 1 && itemsetCount[(curNode.label - 1) * curNode.label
					/ 2 + sibling.label] >= minSupport)) {
				IntegerByRef sameCountTemp = new IntegerByRef();
				sameCountTemp.count = sameCount;
				lastChild = is2_itemSetValid(curNode, sibling, level,
						lastChild, sameCountTemp);
				sameCount = sameCountTemp.count;
			} else if (level > 1) {
				IntegerByRef sameCountTemp = new IntegerByRef();
				sameCountTemp.count = sameCount;
				lastChild = iskItemSetFreq(curNode, sibling, level, lastChild,
						sameCountTemp);
				sameCount = sameCountTemp.count;
			}
			sibling = sibling.next;
		}
		resultCount += Math.pow(2.0, sameCount);
		nlLenSum += Math.pow(2.0, sameCount) * curNode.NLLength;

		result[resultLen++] = curNode.label;

		// ============= Write itemset(s) to file ===========
		writeItemsetsToFile(curNode, sameCount);

		// ======== end of write to file

		nlNodeCount++;

		int from_cursor = bf_cursor;
		int from_col = bf_col;
		int from_size = bf_currentSize;
		NodeListTreeNode child = curNode.firstChild;
		NodeListTreeNode next = null;
		while (child != null) {
			next = child.next;
			traverse(child, curNode, level + 1, sameCount);
			for (int c = bf_col; c > from_col; c--) {
				bf[c] = null;
			}
			bf_col = from_col;
			bf_cursor = from_cursor;
			bf_currentSize = from_size;
			child = next;
		}
		resultLen--;
	}

	NodeListTreeNode is2_itemSetValid(NodeListTreeNode ni, NodeListTreeNode nj,
			int level, NodeListTreeNode lastChild, IntegerByRef sameCount) {
		int i = ni.label;
		int j = nj.label;
		if (ni.support == itemsetCount[(i - 1) * i / 2 + j]) {
			sameItems[sameCount.count++] = nj.label;
		} else {
			NodeListTreeNode nlNode = new NodeListTreeNode();
			nlNode.label = j;
			nlNode.NLCol = nlistCol;
			nlNode.NLStartinBf = nlistBegin[(i - 1) * i / 2 + j];
			nlNode.NLLength = nlistLen[(i - 1) * i / 2 + j];
			nlNode.support = itemsetCount[(i - 1) * i / 2 + j];
			nlNode.firstChild = null;
			nlNode.next = null;
			if (ni.firstChild == null) {
				ni.firstChild = nlNode;
				lastChild = nlNode;
			} else {
				lastChild.next = nlNode;
				lastChild = nlNode;
			}
		}
		return lastChild;
	}

	/**
	 * This method write an itemset to file + all itemsets that can be made
	 * using its node list.
	 * 
	 * @param curNode
	 *            the current node
	 * @param sameCount
	 *            the same count
	 * @throws IOException
	 *             exception if error reading/writting to file
	 */
	private void writeItemsetsToFile(NodeListTreeNode curNode, int sameCount)
			 {
        
	
		if(curNode.support >= minSupport) {
			outputCount++;
			// append items from the itemset to the StringBuilder
			if (maxlength < resultLen){
				
			path.clear();
			String str = "";
			for (int i = 0; i < resultLen; i++) {
				str =  (item[result[i]].index)+","+ str;
			}
			maxlength = resultLen;
			path.add(str);
			}
			
		}
		
		// === Write all combination that can be made using the node list of
		// this itemset
		if (sameCount > 0) {
			if (maxlength < resultLen){
				
				path.clear();
				String str = "";
			// generate all subsets of the node list except the empty set
			for (long i = 1, max = 1 << sameCount; i < max; i++) {
				
				for (int k = 0; k < resultLen; k++) {
					str =  item[result[k]].index + ","+str;
					
				}

				// we create a new subset
				for (int j = 0; j < sameCount; j++) {
					// check if the j bit is set to 1
					int isSet = (int) i & (1 << j);
					if (isSet > 0) {
						// if yes, add it to the set
						str =   item[sameItems[j]].index + ","+str;
						
						// newSet.add(item[sameItems[j]].index);
					}
				}
				
				outputCount++;
			}
			maxlength = resultLen;
			path.add(str);
		}
		}
		
	}
	

	

	/** Class to pass an integer by reference as in C++
	 */
	class IntegerByRef {
		int count;
	}

	class Item {
		public String index;
		public int num;
		//public String rule;
	}

	class NodeListTreeNode {
		public int  label;
		public NodeListTreeNode firstChild;
		public NodeListTreeNode next;
		public int support;
		public int NLStartinBf;
		public int NLLength;
		public int NLCol;
	}

	class PPCTreeNode {
		public int label;
		public PPCTreeNode firstChild;
		public PPCTreeNode rightSibling;
		public PPCTreeNode father;
		public int count;
		public int foreIndex;
	}
}
