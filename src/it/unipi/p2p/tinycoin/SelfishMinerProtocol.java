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
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class SelfishMinerProtocol implements CDProtocol, EDProtocol {
	
	private static final String PAR_MAX_TRANS_BLOCK = "max_trans_block";
	private static final String PAR_REWARD = "reward";
	private static final String PAR_NODE_PROT = "node_protocol";

	private int minedBlocks;
	private boolean selected;
	private int maxTransPerBlock;
	private double reward;
	private int nodeProtocol;
	private List<Block> privateBlockchain; 
	private int privateBranchLength;
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public SelfishMinerProtocol(String prefix) {
		minedBlocks = 0;
		maxTransPerBlock = Configuration.getInt(prefix + "." + PAR_MAX_TRANS_BLOCK);
		reward = Configuration.getDouble(prefix + "." + PAR_REWARD);
		nodeProtocol = Configuration.getPid(prefix + "." + PAR_NODE_PROT);
		privateBlockchain = new ArrayList<>();
		privateBranchLength = 0;
	}
	
	public void setPrivateBranchLength(int privateBranchLength) {
		this.privateBranchLength = privateBranchLength;
	}

	public void setPrivateBlockchain(List<Block> privateBlockchain) {
		this.privateBlockchain = privateBlockchain;
	}

	@Override
	public Object clone() {
		SelfishMinerProtocol smp = null;
		try {
			smp = (SelfishMinerProtocol)super.clone();
			smp.setMinedBlocks(0);
			smp.setSelected(false);
			smp.setMaxTransPerBlock(maxTransPerBlock);
			smp.setReward(reward);
			smp.setNodeProtocol(nodeProtocol);
			smp.setPrivateBlockchain(new ArrayList<>());
			smp.setPrivateBranchLength(0);
			
		}
		catch(CloneNotSupportedException  e) {
			
		}
		return smp;
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
		if (isSelected()) 
		{
			setSelected(false);
			TinyCoinNode tnode = (TinyCoinNode)node;
			Map<String, Transaction> transPool = tnode.getTransPool();
			
			// Create a new block
			Block b = createBlock(transPool, tnode);
			String last = privateBlockchain.size()==0 ? null : privateBlockchain.get(privateBlockchain.size()-1).getBid();
			
			if (isValidBlock(last, b)) {								
				// Announce the block either to the selfish miners or to all the neighbor nodes based on convenience
				List<Block> blockchain = tnode.getBlockchain();
				int prevDifference = privateBlockchain.size() - blockchain.size();
				privateBlockchain.add(b);
				privateBranchLength ++;
				// If there was a fork, publish both blocks of the private branch to win the tie break
				if (prevDifference == 0 && privateBranchLength == 2 ) {
					copyPrivateBlockchain(tnode); // TODO my assumption, but is it correct?
					for (int i = privateBranchLength; i > 0; i--) 
						sendBlockToNeighbors(node, nodeProtocol, privateBlockchain.get(privateBlockchain.size()-i));
					privateBranchLength = 0;				
				}
				else  //TODO: added this 'else', to be checked
					sendBlockToSelfishMiners(node, pid, b);			
				System.out.println("Mined a block!" );
			}			
		}		
	}
	
	public boolean isValidBlock(String last, Block toBeAdded) {
		if ( last != toBeAdded.getParent()) {
			try {
				throw new Exception("Parent node of the new block is different from the last"
						+ "node of the blockchain");
			} catch (Exception e) {
				//e.printStackTrace();         //Exception put for debug purposes, it is fired everytime an already received block is received
				return false;
			}
		}
		return true;
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
	
	
	/** Sends a block b to the protocol pid of the neighbor nodes which are selfish miners
	 * 
	 * @param sender The sender node
	 * @param pid The id of the protocol the message is directed to
	 * @param b The block to be sent
	 */
	public void sendBlockToSelfishMiners(Node sender, int pid, Block b) {		
		int linkableID = FastConfig.getLinkable(pid);
		Linkable linkable = (Linkable) sender.getProtocol(linkableID);
			for (int i =0; i<linkable.degree(); i++) {
				TinyCoinNode peer = (TinyCoinNode)linkable.getNeighbor(i);
				if ( peer.isSelfishMiner())
				    ((Transport)sender.getProtocol(FastConfig.getTransport(pid)))
				    .send(sender, peer, b, pid);
			}
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		// This protocol only receives blocks sent by selfish miners. The blocks have to be added to the private blockchain
		TinyCoinNode tnode = (TinyCoinNode)node;
		List<Block> blockchain = tnode.getBlockchain();
		int prevDifference = privateBlockchain.size() - blockchain.size();
		Block b = (Block)event;
		String last = privateBlockchain.size()==0 ? null : privateBlockchain.get(privateBlockchain.size()-1).getBid();
		if (isValidBlock(last, b)) {
			privateBlockchain.add(b);
			privateBranchLength++;
			if (prevDifference == 0 && privateBranchLength == 2 ) {
				copyPrivateBlockchain(tnode);
				for (int i = privateBranchLength; i > 0; i--) 
					sendBlockToNeighbors(node, nodeProtocol, privateBlockchain.get(privateBlockchain.size()-i));
				privateBranchLength = 0;				
			}
			sendBlockToSelfishMiners(node, pid, b);	
		}				
	}
	
	
	//TODO: 'merged' tnode and node in tnode, should work but test
	public Block createBlock(Map<String, Transaction> transPool, TinyCoinNode tnode) {
		minedBlocks++;
		int transInBlock = Math.min(transPool.size(), maxTransPerBlock);
		String bid = "B" + tnode.getID() + minedBlocks;			
		String parent = privateBlockchain.size()== 0 
				? null : privateBlockchain.get(privateBlockchain.size()-1).getBid();
		List<Transaction> trans = new ArrayList<>(transInBlock);
		//TODO: catch 'NoSUchElementException' in case TransPool is empty (cycle 1)
		Iterator<String> iter = tnode.getTransPool().keySet().iterator();
		for (int i=0; i< transInBlock; i++) {
			String key = iter.next();
			Transaction t = transPool.get(key);
			iter.remove();
			trans.add(t);
			if (t.getOutput() == tnode) {  //TODO check if test works
				tnode.increaseBalance(t.getAmount());
			}
		}
		return new Block(bid, parent, tnode, trans, reward);	
	}
	
	public List<Block> getPrivateBlockchain() {
		return privateBlockchain;
	}

	public int getPrivateBranchLength() {
		return privateBranchLength;
	}

	/** Update the public blockchain to be a copy of the private one, discarding the last item of the public one
	 * 
	 */
	public void copyPrivateBlockchain(TinyCoinNode tnode) {
		List<Block> blockchain = tnode.getBlockchain();
		blockchain.remove(blockchain.size() - 1); //remove last item   //TODO also remove fees
		for (int i = privateBranchLength; i > 0; i--) {
			Block b = privateBlockchain.get(privateBlockchain.size() - i);
			blockchain.add(b);         //TODO add fees of the new blocks
		}
	}
	
	/** Update the private blockchain to be a copy of the public one, discarding the last item of the private one
	 * 
	 */
	public void copyPublicBlockchain(TinyCoinNode tnode) {
		List<Block> blockchain = tnode.getBlockchain();
		if (privateBlockchain.size() != 0)
			privateBlockchain.remove(privateBlockchain.size() - 1); //remove last item
		for (int i = privateBranchLength; i >= 0; i--) {
			Block b = blockchain.get(blockchain.size() - (i+1));
			privateBlockchain.add(b);
		}
	}

}
