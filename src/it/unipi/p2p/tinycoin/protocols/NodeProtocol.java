package it.unipi.p2p.tinycoin.protocols;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unipi.p2p.tinycoin.Block;
import it.unipi.p2p.tinycoin.TinyCoinNode;
import it.unipi.p2p.tinycoin.Transaction;
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
	private static final String PAR_SMINER = "self_miner_prot";
	
	private double transProb;
	private int numTrans;
	private int smpid;
	private boolean fork;
	private Block forked;
	private int numForks;
	

	public int getNumForks() {
		return numForks;
	}

	public void setSmpid(int smpid) {
		this.smpid = smpid;
	}

	public void setNumTrans(int numTrans) {
		this.numTrans = numTrans;
	}

	public NodeProtocol(String prefix) 
	{
		transProb = Configuration.getDouble(prefix + "." + PAR_P_TRANS);
		numTrans = 0;		
		smpid = Configuration.getPid(prefix + "." + PAR_SMINER);
		fork = false;
		forked = null;
		numForks = 0;
	}
	
	public void setFork(boolean fork) {
		this.fork = fork;
	}

	public void setForked(Block forked) {
		this.forked = forked;
	}

	@Override
	public Object clone() {
		NodeProtocol np = null;
		try {
			np = (NodeProtocol)super.clone();
			np.setTransProb(transProb);
			np.setNumTrans(0);
			np.setSmpid(smpid);
			np.setFork(false);
			np.setForked(null);
			np.setNumForks(0);
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
			tnode.getTransPool().put(tid, t);
			balance -= totalSpent;
			
			// Send the transaction to all neighbor nodes
			sendTransactionToNeighbors(node, pid, t);			
		}		
	}
	
	// TODO: @Runtime, differentiate whether Object is Transaction or Block 
	@Override
	public void processEvent(Node node, int pid, Object event)
	{
		if (event instanceof Transaction) {
			
			Transaction t = (Transaction) event;
			Map<String, Transaction> transPool = ((TinyCoinNode) node).getTransPool();
			// If never received the transaction, broadcast it to the neighbors
			String tid = t.getTid();
			if (!transPool.containsKey(tid)) {
				//System.out.println("Node " + node.getID() + " received transaction " + tid);
				transPool.put(tid, t);
				sendTransactionToNeighbors(node, pid, t);
			}
		}
			
		
		else if (event instanceof Block) {
			TinyCoinNode tnode = (TinyCoinNode)node;	
			List<Block> blockchain = tnode.getBlockchain();
			Block b = (Block)event;	
			String last = blockchain.size()==0 ? null : blockchain.get(blockchain.size()-1).getBid();
			
			if (tnode.isSelfishMiner()) {  //Selfish miner receives a new block from a honest node
				SelfishMinerProtocol smp = (SelfishMinerProtocol)node.getProtocol(smpid);		
				List<Block> privateBlockchain = smp.getPrivateBlockchain();
				int privateBranchLength = smp.getPrivateBranchLength();
				int prevDiff = privateBlockchain.size() - blockchain.size();
				if ( last == b.getParent()) {
					blockchain.add(b);
					switch (prevDiff) {
					case(0):
						if (onlyAddTheBlock(privateBlockchain, blockchain))
							privateBlockchain.add(b);             //simply add one block
						else 
							smp.copyPublicBlockchain(tnode);      // delete last block of private blockchain to make the two exactly equal
						
						smp.setPrivateBranchLength(0);
						sendBlockToNeighbors(node, pid, b);
						/*
						if (privateBranchLength == 0) {
							//blockchain.add(b);
							privateBlockchain.add(b);
							 // Send to every node because the other self miners should immediately start mining on the new block
							sendBlockToNeighbors(node, pid, b);
						}
						else { //TODO: qualcosa non va qui : 
							smp.copyPublicBlockchain(tnode);      
							smp.setPrivateBranchLength(0);
							sendBlockToNeighbors(node, pid, b);
						}		
						*/				
						break;
					case(1): 
						Block sb = privateBlockchain.get(privateBlockchain.size() - 1);
				     	sendBlockToNeighbors(node, pid, sb); 						
						break;
					case(2):
						for (int i = privateBranchLength; i > 0; i--) {
							sb = privateBlockchain.get(privateBlockchain.size() - i);
							sendBlockToNeighbors(node, pid, sb); 
						}
						smp.copyPrivateBlockchain(tnode); //TODO: it is node's assumption, but is it ok?					
						smp.setPrivateBranchLength(0);						
						break;
					default:
						sb = privateBlockchain.get(privateBlockchain.size() - privateBranchLength);
						sendBlockToNeighbors(node, pid, sb); 
						//blockchain.add(sb);
						smp.setPrivateBranchLength(privateBranchLength - 1);
						break;
					}
				}
			}
			
			else {
				// If the parent field of the block is valid, then the honest miner adds the block 
				// to its blockchain and removes the transactions inside the block from the pool.				
				if ( last == b.getParent() ||
						(fork == true && forked.getBid() == b.getParent())) {
					if (fork == true) {													
						if (forked.getBid() == b.getParent()) {
							blockchain.remove(blockchain.size()-1); //TODO: remove reward
							blockchain.add(forked);
						}
						fork = false;  // Fork is resolved, regardless of which is the extended branch
						forked = null;
					}
					blockchain.add(b);
					Map<String, Transaction> transPool = tnode.getTransPool();
					for (Transaction t : b.getTransactions()) {
						// If this node is the recipient, update its balance
						if (t.getOutput() == node) {  //TODO check if test works
							tnode.increaseBalance(t.getAmount());
						}
						transPool.remove(t.getTid());
					}
					
				// Finally (if block is valid) send the block to all  the neighbor nodes				
					sendBlockToNeighbors(node, pid, b);						
			    }
				else if (blockchain.size() >= 2 && 
						blockchain.get(blockchain.size()-2).getBid() == b.getParent() &&
						blockchain.get(blockchain.size()-1).getBid() != b.getBid() && 
						fork == false) {
					fork = true;
					forked = b;
					numForks++;
					sendBlockToNeighbors(node, pid, b);	//TODO: added line, must check
				}
				
			}
			
			
		}
		
	}
		
	
	
	public void setNumForks(int numForks) {
		this.numForks = numForks;
	}

	/** Sends a transaction t to the protocol pid of all the neighbor nodes
	 * 
	 * @param sender The sender node
	 * @param pid The id of the protocol the message is directed to
	 * @param t The transaction to be sent
	 */
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
	
	
	/** Sends a block b to the protocol pid of all the neighbor nodes
	 * 
	 * @param sender The sender node
	 * @param pid The id of the protocol the message is directed to
	 * @param b The block to be sent
	 */
	public void sendBlockToNeighbors(Node sender, int pid, Block b) {		
		int linkableID = FastConfig.getLinkable(pid);
		Linkable linkable = (Linkable) sender.getProtocol(linkableID);
			for (int i =0; i<linkable.degree(); i++) {
				Node peer = linkable.getNeighbor(i);
				((Transport)sender.getProtocol(FastConfig.getTransport(pid)))
				.send(sender, peer, b, pid);
			}
	}
	
	public double getTransProb() {
		return transProb;
	}

	public void setTransProb(double transProb) {
		this.transProb = transProb;
	}
	
	private boolean onlyAddTheBlock(List<Block> privateBlockchain , List<Block> blockchain ) 
	{
		if (privateBlockchain.size() == 0 ||
				blockchain.get(blockchain.size() -1).getParent() == privateBlockchain.get(privateBlockchain.size()-1).getBid())
			return true;
		else
			return false;
	}

}
