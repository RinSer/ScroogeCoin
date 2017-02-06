import java.util.*;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */	
	private UTXOPool ledger;
	
	public TxHandler(UTXOPool utxoPool) {
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

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	Transaction[] valid_transactions = new Transaction[possibleTxs.length];
    	int valid_transaction_index = 0;
    	for (Transaction current_transaction : possibleTxs) {
    		if (this.isValidTx(current_transaction)) {
    			// Update the current UTXO pool
    			// Remove the used utxos
    			for (int j = 0; j < current_transaction.numInputs(); j++) {
    				Transaction.Input current_input = current_transaction.getInput(j);
    				UTXO current_input_utxo = new UTXO(current_input.prevTxHash, current_input.outputIndex);
    				ledger.removeUTXO(current_input_utxo);
    			}
    			// Add the emerged utxos
    			for (int i = 0; i < current_transaction.numOutputs(); i++) {
    				Transaction.Output current_output = current_transaction.getOutput(i);
    				UTXO current_utxo = new UTXO(current_transaction.getHash(), i);
    				ledger.addUTXO(current_utxo, current_output);
    			}
    			// Add the current transaction to the valid list
    			valid_transactions[valid_transaction_index] = current_transaction;
    			valid_transaction_index++;
    		}
    	}
    	return valid_transactions;
    }

}
