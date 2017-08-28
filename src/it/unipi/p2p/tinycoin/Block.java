package it.unipi.p2p.tinycoin;

import java.util.ArrayList;
import java.util.List;

public class Block {
	
	private final String bid;
	private final String parent;  
	private final TinyCoinNode miner; 
	private final double reward;
	private final List<Transaction> transactions;
	
	public Block(String bid, String parent, TinyCoinNode miner, List<Transaction> trans,
			double fixedFee) 
	{
		this.bid = bid;
		this.parent = parent;
		this.miner = miner;
		transactions = new ArrayList<>();
		double fees = 0;
		for (Transaction t : trans) {
			transactions.add(t);
			fees += t.getFee();
		}
		reward = fixedFee + fees;		
	}

	public String getBid() {
		return bid;
	}

	public String getParent() {
		return parent;
	}

	public TinyCoinNode getMiner() {
		return miner;
	}

	public double getReward() {
		return reward;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}
	
	/** Gets the revenue for the block, defined as the fixed reward plus the fees for all
	 *  the transactions 
	 */
	public double getRevenueForBlock() {
		double revenue = reward;
		List<Transaction> trans = transactions;
		for (Transaction t: trans)
			revenue += t.getFee();
		return revenue;
	}
	
	/** Gets the amount of coins destined to the TinyCoinNode tn in the transactions of the Block.
	 */
	public double getTransactionsAmountIfRecipient(TinyCoinNode tn)
	{
		int amount = 0;		
		for (Transaction t: transactions) {
			if (t.getOutput() == tn)
				amount += t.getAmount();
		}
		return amount;		
	}	

}
