package it.unipi.p2p.tinycoin;

import java.util.ArrayList;
import java.util.List;

public class Block {
	
	private final String bid;
	private final String parent;  // id del blocco padre
	private final TinyCoinNode miner; // il miner che ha minato il blocco
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
	

}
