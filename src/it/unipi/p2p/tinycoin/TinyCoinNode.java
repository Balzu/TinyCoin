package it.unipi.p2p.tinycoin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.core.GeneralNode;

public class TinyCoinNode extends GeneralNode{
	
	private MinerType mtype;
	private boolean node;	
	private double balance;
	private List<Block> blockchain; 
    private Map<String, Transaction> transPool;
	

	public void setTransPool(Map<String, Transaction> transPool) {
		this.transPool = transPool;		
	}
	

	public MinerType getMtype() {
		return mtype;
	}

	public void setMtype(MinerType mtype) {
		this.mtype = mtype;
	}

	public boolean isNode() {
		return node;
	}
	
	public boolean isMiner() {
		return !isNode();
	}

	public void setNode(boolean node) {
		this.node = node;
	}
	

	public TinyCoinNode(String prefix) {
		super(prefix);
		transPool = new HashMap<>();
		blockchain = new ArrayList<>();
	}
	
	
	public List<Block> getBlockchain() {
		return blockchain;
	}


	public void setBlockchain(List<Block> blockchain) {
		this.blockchain = blockchain;
	}


	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public void increaseBalance(double amount) {
		balance += amount;
	}

	@Override
	public Object clone()
	{
		TinyCoinNode clone = (TinyCoinNode)super.clone();
		//TODO: devo lasciarli i 2 metodi sotto o non servono?
		clone.setMtype(this.getMtype());
		clone.setNode(this.isNode());
		clone.setTransPool(new HashMap<>());	//TODO ok inizializzarne una nuova? 
		clone.setBlockchain(new ArrayList<>());
		return clone;
	}


	public Map<String, Transaction> getTransPool() {
		return transPool;
	}

}
