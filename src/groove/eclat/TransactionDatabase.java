package groove.eclat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TransactionDatabase {
	// The list of items in this database
		private final Set<Integer> items = new HashSet<Integer>();
		// the list of transactions
		private final List<List<Integer>> transactions = new ArrayList<List<Integer>>();

		/**
		 * Method to add a new transaction to this database.
		 * @param transaction  the transaction to be added
		 */
		public void addTransaction(List<Integer> transaction) {
			transactions.add(transaction);
			items.addAll(transaction);
		}

		/**
		 * Method to load a file containing a transaction database into memory
		 * @param path the path of the file
		 * @throws IOException exception if error reading the file
		 */
		public void loadFile(String path) throws IOException {
			String thisLine; // variable to read each line
			BufferedReader myInput = null; // object to read the file
			try {
				FileInputStream fin = new FileInputStream(new File(path));
				myInput = new BufferedReader(new InputStreamReader(fin));
				// for each line
				while ((thisLine = myInput.readLine()) != null) {
					// if the line is not a comment, is not empty or is not other
					// kind of metadata
					if (thisLine.isEmpty() == false &&
							thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
							&& thisLine.charAt(0) != '@') {
						// split the line according to spaces and then
						// call "addTransaction" to process this line.
						addTransaction(thisLine.split(" "));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (myInput != null) {
					myInput.close();
				}
			}
		}

		/**
		 * This method process a line from a file that is read.
		 * @param tokens the items contained in this line
		 */
		public void addTransaction(String itemsString[]) {
			// create an empty transaction
			List<Integer> itemset = new ArrayList<Integer>();
			// for each item in this line
			for (String attribute : itemsString) {
				// convert from string to int
				int item = Integer.parseInt(attribute);
				// add the item to the current transaction
				itemset.add(item); 
				// add the item to the set of all items in this database
				items.add(item);
			}
			// add the transactions to the list of all transactions in this database.
			transactions.add(itemset);
		}

		/**
		 * Method to print the content of the transaction database to the console.
		 */
		public void printDatabase() {
			System.out
					.println("===================  TRANSACTION DATABASE ===================");
			int count = 0; 
			// for each transaction
			for (List<Integer> itemset : transactions) { // pour chaque objet
				System.out.print(count + ":  ");
				print(itemset); // print the transaction 
				count++;
			}
		}
		
		/**
		 * Method to print a transaction to System.out.
		 * @param itemset a transaction
		 */
		private void print(List<Integer> itemset){
			StringBuilder r = new StringBuilder();
			// for each item in this transaction
			for (Integer item : itemset) {
				// append the item to the StringBuilder
				r.append(item.toString());
				r.append(' ');
			}
			System.out.println(r); // print to System.out
		}

		/**
		 * Get the number of transactions in this transaction database.
		 * @return the number of transactions.
		 */
		public int size() {
			return transactions.size();
		}

		/**
		 * Get the list of transactions in this database
		 * @return A list of transactions (a transaction is a list of Integer).
		 */
		public List<List<Integer>> getTransactions() {
			return transactions;
		}

		/**
		 * Get the set of items contained in this database.
		 * @return The set of items.
		 */
		public Set<Integer> getItems() {
			return items;
		}
		/////////////////////////////////////////////////////////
		public TransactionDatabase(ArrayList<String> allpath, Map<String, Integer> mappedRules){
			for (int i=0; i<=allpath.size()-1; i++){
				String[] path = allpath.get(i).split(",");
				List<Integer> mappedPath = new ArrayList<Integer>();
				for(String rule: path){
					mappedPath.add(mappedRules.get(rule));
				}
				addTransaction(mappedPath);
			}
		}

		public TransactionDatabase() {
			// TODO Auto-generated constructor stub
		}

}
