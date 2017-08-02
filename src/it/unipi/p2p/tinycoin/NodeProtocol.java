package it.unipi.p2p.tinycoin;

import java.util.HashMap;
import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class NodeProtocol implements CDProtocol, EDProtocol{
	
	private static final String PAR_P_TRANS = "transaction_prob";
	
	private double transProb;
	private int numTrans;
	private Map<String, Transaction> transPool;
	

	public void setTransPool(Map<String, Transaction> transPool) {
		this.transPool = transPool;
	}

	public void setNumTrans(int numTrans) {
		this.numTrans = numTrans;
	}

	public NodeProtocol(String prefix) 
	{
		transProb = Configuration.getDouble(prefix + "." + PAR_P_TRANS);
		numTrans = 0;
		transPool = new HashMap<>();
	}
	
	@Override
	public Object clone() {
		NodeProtocol np = null;
		try {
			np = (NodeProtocol)super.clone();
			np.setTransProb(transProb);
			np.setNumTrans(0);
			np.setTransPool(new HashMap<>());	//TODO ok inizializzarne una nuova? 		
		}
		catch(CloneNotSupportedException  e) {
			
		}
		return np;
	}
	

	@Override
	public void nextCycle(Node node, int pid)
	{
		TinyCoinNode tnode = (TinyCoinNode) node;
		double balance = tnode.getBalance();
		// Completely arbitrary, I assume that if a node has less than 1 coin cannot make a transaction
		// (Substitutes the test for empty balance, and allow to avoid very small, fractional transactions)
		if (balance < 1) {  
			return;
		}
		double r = Math.random();
		// At each cycle, each node generates a transaction with a given probability
		if (r < transProb) { 
			
			String tid = node.getID() + "@" + numTrans;
			numTrans++;
			
			// Randomly choose one recipient
			Network.shuffle();
			TinyCoinNode recipient = (TinyCoinNode) Network.get(0);
			
			double totalSpent = Math.random() * balance;
			double amount = totalSpent * (9.0/10.0);
			double fee = totalSpent - amount;
			
			Transaction t = new Transaction(tid, tnode, recipient, amount, fee);
			System.out.println(t.toString());
			// Transaction has been created, so update balance and insert into local pool of unconfirmed transactions
			transPool.put(tid, t);
			balance -= totalSpent;
			
			// Send the transaction to all neighbor nodes
			sendTransactionToNeighbors(node, pid, t);			
		}		
	}
	
	// TODO: @Runtime, differentiate whether Object is Transaction or Block 
	@Override
	public void processEvent(Node node, int pid, Object event)
	{
		Transaction t = (Transaction) event;
		
		// If never received the transaction, broadcast it to the neighbors
		String tid = t.getTid();
		if (!transPool.containsKey(tid)) {
			//System.out.println("Node " + node.getID() + " received transaction " + tid);
			transPool.put(tid, t);
			sendTransactionToNeighbors(node, pid, t);
		}
	}
		
	
	
	public void sendTransactionToNeighbors(Node sender, int pid, Transaction t) {
		// Send the transaction to all neighbor nodes
		int linkableID = FastConfig.getLinkable(pid);
		Linkable linkable = (Linkable) sender.getProtocol(linkableID);
			for (int i =0; i<linkable.degree(); i++) {
				Node peer = linkable.getNeighbor(i);
				((Transport)sender.getProtocol(FastConfig.getTransport(pid))) //TODO: set Transport class
				.send(sender, peer, t, pid);
	}
		
		
		
	}
	
	public double getTransProb() {
		return transProb;
	}

	public void setTransProb(double transProb) {
		this.transProb = transProb;
	}

}
