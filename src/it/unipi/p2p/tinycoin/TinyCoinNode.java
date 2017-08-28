package it.unipi.p2p.tinycoin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.core.GeneralNode;

public class TinyCoinNode extends GeneralNode{
	
	
	private NodeType nodeType;
	private MinerType minerType;
	private double balance;
	private List<Block> blockchain; 
        private Map<String, Transaction> transPool;
        
        
    public TinyCoinNode(String prefix) {
    	super(prefix);
    	transPool = new HashMap<>();
    	blockchain = new ArrayList<>();
    }
        
    @Override
    public Object clone()
    {
    	TinyCoinNode clone = (TinyCoinNode)super.clone();
    	clone.setTransPool(new HashMap<>());
    	clone.setBlockchain(new ArrayList<>());
    	return clone;
    }
	

	public void setTransPool(Map<String, Transaction> transPool) {
		this.transPool = transPool;		
	}
	

	public MinerType getMtype() {
		return minerType;
	}

	public void setMtype(MinerType mtype) {
		this.minerType = mtype;
	}

	public boolean isNode() {
		return nodeType==NodeType.NODE;
	}
	
	public boolean isMiner() {
		return nodeType==NodeType.MINER;
	}
	
	public boolean isSelfishMiner() {
		return nodeType==NodeType.SELFISH_MINER;
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
	
	public void decreaseBalance(double amount) {
		balance -= amount;
	}	


	public void setNodetype(NodeType ntype) {
		this.nodeType = ntype;
	}


	public Map<String, Transaction> getTransPool() {
		return transPool;
	}

}
