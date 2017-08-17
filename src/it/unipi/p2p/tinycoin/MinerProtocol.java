package it.unipi.p2p.tinycoin;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.Transport;

//TODO: must add a method to reach consensus
public class MinerProtocol implements CDProtocol{
	
	private static final String PAR_MAX_TRANS_BLOCK = "max_trans_block";
	private static final String PAR_REWARD = "reward";
	private static final String PAR_NODE_PROT = "node_protocol";

	private int minedBlocks;
	private boolean selected;
	private int maxTransPerBlock;
	private double reward;
	private int nodeProtocol;
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public MinerProtocol(String prefix) {
		minedBlocks = 0;
		maxTransPerBlock = Configuration.getInt(prefix + "." + PAR_MAX_TRANS_BLOCK);
		reward = Configuration.getDouble(prefix + "." + PAR_REWARD);
		nodeProtocol = Configuration.getPid(prefix + "." + PAR_NODE_PROT);
	}
	
	@Override
	public Object clone() {
		MinerProtocol mp = null;
		try {
			mp = (MinerProtocol)super.clone();
			mp.setMinedBlocks(0);
			mp.setSelected(false);
			mp.setMaxTransPerBlock(maxTransPerBlock);
			mp.setReward(reward);
			mp.setNodeProtocol(nodeProtocol);
			
		}
		catch(CloneNotSupportedException  e) {
			
		}
		return mp;
	}
	
	public void setMaxTransPerBlock(int maxTransPerBlock) {
		this.maxTransPerBlock = maxTransPerBlock;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	public void setNodeProtocol(int nodeProtocol) {
		this.nodeProtocol = nodeProtocol;
	}

	public int getMinedBlocks() {
		return minedBlocks;
	}

	public void setMinedBlocks(int minedBlocks) {
		this.minedBlocks = minedBlocks;
	}

	@Override
	public void nextCycle(Node node, int pid)
	{
		TinyCoinNode tnode = (TinyCoinNode)node;
		
		if (!tnode.isMiner())
			return;
		
		if (isSelected()) 
		{
			setSelected(false);
			
			Map<String, Transaction> transPool = tnode.getTransPool();
			
			// Create a new block and announce it to all the protocols of all the neighbors
			minedBlocks++;
			int transInBlock = Math.min(transPool.size(), maxTransPerBlock);
			List<Block> blockchain = tnode.getBlockchain();
			String bid = "B" + node.getID() + minedBlocks;			
			String parent = blockchain.size()== 0 
					? null : blockchain.get(blockchain.size()-1).getBid();
			List<Transaction> trans = new ArrayList<>(transInBlock);
			Iterator<String> iter = tnode.getTransPool().keySet().iterator();
			for (int i=0; i< transInBlock; i++) {
				String key = iter.next();
				Transaction t = transPool.get(key);
				iter.remove();
				trans.add(t);
				if (t.getOutput() == node) {  //TODO check if test works
					tnode.increaseBalance(t.getAmount());
				}
			}
			Block b = new Block(bid, parent, tnode, trans, reward);	
			blockchain.add(b);
			
			sendBlockToNeighbors(node, nodeProtocol, b);	
			
			
			System.out.println("Mined a block!");
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

	/*
	@Override
	public void processEvent(Node node, int pid, Object event)
	{
		// Only process the block with this protocol if this node is a honest miner
		TinyCoinNode tnode = (TinyCoinNode)node;	
		
		if (!tnode.isMiner())
			return;
		
		// If the parent field of the block is valid, then the honest miner adds the block 
		// to its blockchain and removes the transactions inside the block from the pool.
		// In other words, in case of fork only the first block is kept, while the latter is discarded
		Block b = (Block)event;		
		List<Block> blockchain = tnode.getBlockchain();
		String last = blockchain.size()==0 ? null : blockchain.get(blockchain.size()-1).getBid();
		if ( last == b.getParent()) {
			blockchain.add(b);
			Map<String, Transaction> transPool = tnode.getTransPool();
			for (Transaction t : b.getTransactions()) {
				// If this node is the recipient, update its balance
				if (t.getOutput() == node) {  //TODO check if test works
					tnode.increaseBalance(t.getAmount());
				}
				transPool.remove(t.getTid());
			}
			
		// Finally (if block is valid) send the block to all the protocols of the neighbor nodes
			for (pid=0; pid<node.protocolSize(); pid++) {
				sendBlockToNeighbors(node, pid, b);	
			}
			
		}		
	}
	*/
	


}
