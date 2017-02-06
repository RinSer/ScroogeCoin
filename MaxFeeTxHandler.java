import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class MaxFeeTxHandler {
	
	private class FeeTransaction {
		public double fee;
		public Transaction transaction;
		public HashSet<UTXO> utxos;
		
		public FeeTransaction(Transaction transaction) {
			this.transaction = transaction;
			this.fee = this.computeFee(this.transaction);
			this.utxos = this.findUTXOs(this.transaction);
		}
		
		private double computeFee(Transaction transaction) {
			double fee_sum = 0;
			// Check the inputs
	    	for (int i = 0; i < transaction.numInputs(); i++) {
	    		Transaction.Input current_input = transaction.getInput(i);
	    		// Check if previous outputs claimed by the current input is in current UTXO pool (solves (1))
	    		UTXO current_input_utxo = new UTXO(current_input.prevTxHash, current_input.outputIndex); 
	    		//System.out.println(ledger.getAllUTXO().size());
	    		if (ledger.contains(current_input_utxo)) {
	    			// Extract the previous output data
	    			Transaction.Output previous_output = ledger.getTxOutput(current_input_utxo);
	    			// Adjust the total input sum
	    			fee_sum += previous_output.value;
	    		}
	    		
	    	}
	    	// Check the outputs
	    	for (Transaction.Output current_output : transaction.getOutputs()) {
	    		// Adjust the total fee sum
	    		fee_sum -= current_output.value;
	    	}
			return fee_sum;
		}
		
		private void reComputeFee(UTXOPool current_ledger) {
			double fee_sum = 0;
			// Check the inputs
	    	for (int i = 0; i < this.transaction.numInputs(); i++) {
	    		Transaction.Input current_input = this.transaction.getInput(i);
	    		// Check if previous outputs claimed by the current input is in current UTXO pool (solves (1))
	    		UTXO current_input_utxo = new UTXO(current_input.prevTxHash, current_input.outputIndex); 
	    		//System.out.println(ledger.getAllUTXO().size());
	    		if (current_ledger.contains(current_input_utxo)) {
	    			// Extract the previous output data
	    			Transaction.Output previous_output = current_ledger.getTxOutput(current_input_utxo);
	    			// Adjust the total input sum
	    			fee_sum += previous_output.value;
	    		}
	    		
	    	}
	    	// Check the outputs
	    	for (Transaction.Output current_output : this.transaction.getOutputs()) {
	    		// Adjust the total fee sum
	    		fee_sum -= current_output.value;
	    	}
			this.fee = fee_sum;
		}
		
		private HashSet<UTXO> findUTXOs(Transaction transaction) {
			HashSet<UTXO> utxos = new HashSet<UTXO>();
			for (int i = 0; i < transaction.numInputs(); i++) {
				Transaction.Input current_input = transaction.getInput(i);
				utxos.add(new UTXO(current_input.prevTxHash, current_input.outputIndex));
			}
			return utxos;
		}
	}

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */	
	private UTXOPool ledger;
	
	public MaxFeeTxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	ledger = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	double total_input_sum = 0;
    	double total_output_sum = 0;
    	HashSet<UTXO> claimed_utxos = new HashSet<UTXO>();
    	// Check the inputs
    	for (int i = 0; i < tx.numInputs(); i++) {
    		Transaction.Input current_input = tx.getInput(i);
    		// Check if previous outputs claimed by the current input is in current UTXO pool (solves (1))
    		UTXO current_input_utxo = new UTXO(current_input.prevTxHash, current_input.outputIndex); 
    		//System.out.println(ledger.getAllUTXO().size());
    		if (ledger.contains(current_input_utxo)) {
    			// Extract the previous output data
    			Transaction.Output previous_output = ledger.getTxOutput(current_input_utxo);
    			// Check if the current utxo has been already claimed by the transaction (solves (3))
    			if (claimed_utxos.contains(current_input_utxo)) {
    				return false;
    			} else {
    				claimed_utxos.add(current_input_utxo);
    			}
    			// Adjust the total input sum
    			total_input_sum += previous_output.value;
    			// Check if the signature on current input is valid (solves (2))
        		if (!Crypto.verifySignature(previous_output.address, tx.getRawDataToSign(i), current_input.signature)) {
        			return false;
        		}
    		}
    		else {
    			return false;
    		}
    		
    	}
    	// Check the outputs
    	for (int j = 0; j < tx.numOutputs(); j++) {
    		Transaction.Output current_output = tx.getOutput(j);
    		// Check if the current output value is non-negative (solves (4))
    		if (current_output.value < 0) {
    			return false;
    		}
    		// Adjust the total output sum
    		total_output_sum += current_output.value;
    	}
    	// Check if the sum of input values is greater than or equals to the sum of output values (solves (5))
    	if (total_input_sum < total_output_sum) {
    		return false;
    	}
    	// Everything is fine
    	return true;
    }
    
    
    public boolean isValidTxCurrent(Transaction tx, UTXOPool current_ledger) {
        // IMPLEMENT THIS
    	double total_input_sum = 0;
    	double total_output_sum = 0;
    	HashSet<UTXO> claimed_utxos = new HashSet<UTXO>();
    	// Check the inputs
    	for (int i = 0; i < tx.numInputs(); i++) {
    		Transaction.Input current_input = tx.getInput(i);
    		// Check if previous outputs claimed by the current input is in current UTXO pool (solves (1))
    		UTXO current_input_utxo = new UTXO(current_input.prevTxHash, current_input.outputIndex); 
    		//System.out.println(ledger.getAllUTXO().size());
    		if (current_ledger.contains(current_input_utxo)) {
    			// Extract the previous output data
    			Transaction.Output previous_output = current_ledger.getTxOutput(current_input_utxo);
    			// Check if the current utxo has been already claimed by the transaction (solves (3))
    			if (claimed_utxos.contains(current_input_utxo)) {
    				return false;
    			} else {
    				claimed_utxos.add(current_input_utxo);
    			}
    			// Adjust the total input sum
    			total_input_sum += previous_output.value;
    			// Check if the signature on current input is valid (solves (2))
        		if (!Crypto.verifySignature(previous_output.address, tx.getRawDataToSign(i), current_input.signature)) {
        			return false;
        		}
    		}
    		else {
    			return false;
    		}
    		
    	}
    	// Check the outputs
    	for (int j = 0; j < tx.numOutputs(); j++) {
    		Transaction.Output current_output = tx.getOutput(j);
    		// Check if the current output value is non-negative (solves (4))
    		if (current_output.value < 0) {
    			return false;
    		}
    		// Adjust the total output sum
    		total_output_sum += current_output.value;
    	}
    	// Check if the sum of input values is greater than or equals to the sum of output values (solves (5))
    	if (total_input_sum < total_output_sum) {
    		return false;
    	}
    	// Everything is fine
    	return true;
    }
    

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	Transaction[] valid_transactions = new Transaction[possibleTxs.length];
    	int valid_transaction_index = 0;
    	// Find all the conflicting txs
    	ArrayList<FeeTransaction> fee_transactions = new ArrayList<FeeTransaction>();
    	HashMap<UTXO, HashSet<Integer>> used_utxos = new HashMap<UTXO, HashSet<Integer>>();
    	for (int i = 0; i < possibleTxs.length; i++) {
    		Transaction transaction = possibleTxs[i];
    		FeeTransaction current = new FeeTransaction(transaction);
    		for (UTXO used: current.utxos) {
    			if (used_utxos.containsKey(used)) {
    				used_utxos.get(used).add(i);
    			}
    			else {
    				used_utxos.put(used, new HashSet<Integer>());
    				used_utxos.get(used).add(i);
    			}
    		}
    		fee_transactions.add(current);
    	}
    	// Find all conflicting possible sets
    	ArrayList<ArrayList<FeeTransaction>> possible_sets = new ArrayList<ArrayList<FeeTransaction>>();
    	HashSet<Integer> conflict_indices = new HashSet<Integer>();
    	ArrayList<ArrayList<Integer>> conflicting_groups = new ArrayList<ArrayList<Integer>>();
    	//int number_of_possible_sets = 1;
    	for (HashSet<Integer> conflicting : used_utxos.values()) {
    		if (conflicting.size() > 1) {
    			ArrayList<Integer> current_group = new ArrayList<Integer>();
    			for (Integer current : conflicting) {
    				conflict_indices.add(current);
    				current_group.add(current);
    			}
    			conflicting_groups.add(current_group);
    			//number_of_possible_sets *= conflicting.size();
    		}
    	}
    	// Add initial possible set with all unconflicting txs
    	ArrayList<FeeTransaction> current_set = new ArrayList<FeeTransaction>();
    	for (int i = 0; i < fee_transactions.size(); i++) {
    		if (!conflict_indices.contains(i)) {
    			current_set.add(fee_transactions.get(i));
    		}
    	}
    	possible_sets.add(current_set);
    	// Create all the possible tx sets combinations
    	for (ArrayList<Integer> current_group : conflicting_groups) {
    		ArrayList<ArrayList<FeeTransaction>> new_possible_sets = new ArrayList<ArrayList<FeeTransaction>>();
    		for (Integer tx_index : current_group) {
    			for (ArrayList<FeeTransaction> current_possible_set : possible_sets) {
    				ArrayList<FeeTransaction> new_possible_set = new ArrayList<FeeTransaction>();
    				for (FeeTransaction ftx : current_possible_set) {
    					new_possible_set.add(ftx);
    				}
    				if (new_possible_set.size() < tx_index) {
    					new_possible_set.add(fee_transactions.get(tx_index));
    				}
    				else {
    					new_possible_set.add(tx_index, fee_transactions.get(tx_index));
    				}
    				new_possible_sets.add(new_possible_set);
    			}
    		}
    		possible_sets = new_possible_sets;
    	}
    	
    	// Find the best possible set
    	ArrayList<FeeTransaction> best_possible = new ArrayList<FeeTransaction>();
    	double max_sum = 0;
    	for (ArrayList<FeeTransaction> possible_set : possible_sets) {
    		// In each set swap the transactions if one should precede another
    		for (int i = 0; i < possible_set.size(); i++) {
    			for (int j = i+1; j < possible_set.size(); j++) {
    				FeeTransaction first = possible_set.get(i);
    				FeeTransaction second = possible_set.get(j);
    				for (Transaction.Input first_input : first.transaction.getInputs()) {
    					if (Arrays.equals(first_input.prevTxHash, second.transaction.getHash())) {
    						possible_set.set(i, second);
    						possible_set.set(j, first);
    					}
    				}
    			}
    		}
    		UTXOPool current_ledger = new UTXOPool(ledger);
    		double current_sum = 0;
    		ArrayList<FeeTransaction> new_set = new ArrayList<FeeTransaction>();
    		for (FeeTransaction current : possible_set) {
    			current.reComputeFee(current_ledger);
    			if (this.isValidTxCurrent(current.transaction, current_ledger)) {
    				for (int j = 0; j < current.transaction.numInputs(); j++) {
        				Transaction.Input current_input = current.transaction.getInput(j);
        				UTXO current_input_utxo = new UTXO(current_input.prevTxHash, current_input.outputIndex);
        				current_ledger.removeUTXO(current_input_utxo);
        			}
        			// Add the emerged utxos
        			for (int i = 0; i < current.transaction.numOutputs(); i++) {
        				Transaction.Output current_output = current.transaction.getOutput(i);
        				UTXO current_utxo = new UTXO(current.transaction.getHash(), i);
        				current_ledger.addUTXO(current_utxo, current_output);
        			}
        			current_sum += current.fee;
        			new_set.add(current);
    			}
    		}
    		if (current_sum > max_sum) {
    			best_possible = new_set;
    			max_sum = current_sum;
    		}
    	}
    	
    	// Final transactions processing
    	for (FeeTransaction current : best_possible) {
    		if (this.isValidTx(current.transaction)) {
    			// Update the current UTXO pool
    			// Remove the used utxos
    			for (int j = 0; j < current.transaction.numInputs(); j++) {
    				Transaction.Input current_input = current.transaction.getInput(j);
    				UTXO current_input_utxo = new UTXO(current_input.prevTxHash, current_input.outputIndex);
    				ledger.removeUTXO(current_input_utxo);
    			}
    			// Add the emerged utxos
    			for (int i = 0; i < current.transaction.numOutputs(); i++) {
    				Transaction.Output current_output = current.transaction.getOutput(i);
    				UTXO current_utxo = new UTXO(current.transaction.getHash(), i);
    				ledger.addUTXO(current_utxo, current_output);
    			}
    			// Add the current transaction to the valid list
    			valid_transactions[valid_transaction_index] = current.transaction;
    			valid_transaction_index++;
    		}
    	}
    	return valid_transactions;
    }
    
}
