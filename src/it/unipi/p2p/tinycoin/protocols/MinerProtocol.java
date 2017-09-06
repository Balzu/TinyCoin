package it.unipi.p2p.tinycoin.protocols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.unipi.p2p.tinycoin.Block;
import it.unipi.p2p.tinycoin.TinyCoinNode;
import it.unipi.p2p.tinycoin.Transaction;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.Transport;


public class MinerProtocol implements CDProtocol{
	
	private static final String PAR_MAX_TRANS_BLOCK = "max_trans_block";
	private static final String PAR_REWARD = "reward";
	private static final String PAR_NODE_PROT = "node_protocol";

	private int minedBlocks;
	private boolean selected;
	private int maxTransPerBlock;
	private double reward;
	private int nodeProtocol;	

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
			System.err.println(e);
		}
		return mp;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
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
			List<Block> blockchain = tnode.getBlockchain();
			// Create a new block and announce it to all the neighbors			
			Block b = createBlock(transPool, tnode, blockchain);	
			blockchain.add(b);
			tnode.increaseBalance(b.getRevenueForBlock()); //the reward for mining the block is given to the miner
			tnode.increaseBalance(b.getTransactionsAmountIfRecipient(tnode));
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
	
		
	private Block createBlock(Map<String, Transaction> transPool, TinyCoinNode tnode,
			List<Block> blockchain) {
		minedBlocks++;
		int transInBlock = Math.min(transPool.size(), maxTransPerBlock);
		String bid = "B" + tnode.getID() + minedBlocks;			
		String parent = blockchain.size()== 0 
				? null : blockchain.get(blockchain.size()-1).getBid();
		List<Transaction> trans = new ArrayList<>(transInBlock);
		Iterator<String> iter = tnode.getTransPool().keySet().iterator();
		for (int i=0; i< transInBlock; i++) {
			String key = iter.next();
			Transaction t = transPool.get(key);
			iter.remove();
			trans.add(t);			
		}
		return new Block(bid, parent, tnode, trans, reward);	
	}
}
