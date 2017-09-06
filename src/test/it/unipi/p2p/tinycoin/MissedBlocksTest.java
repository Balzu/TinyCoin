package test.it.unipi.p2p.tinycoin;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import it.unipi.p2p.tinycoin.Block;
import it.unipi.p2p.tinycoin.TinyCoinNode;
import it.unipi.p2p.tinycoin.Transaction;
import peersim.core.Node;

public class MissedBlocksTest {
	
	private boolean fork;
	private Block forked;
	private List<Block> missedBlocks;
	private int limit;
	List<Block> blockchain;
	
	public MissedBlocksTest() 
	{		
		fork = false;
		forked = null;	
		missedBlocks = new ArrayList<>();
		limit = 20;
		blockchain = new ArrayList<>();
	}	

	@Test
	public void testWrongArrivalOrder() {		
		Block first = new Block("b1", null, null, new ArrayList<Transaction>(), 0); // only the order of blocks is under test
		Block second = new Block("b2", "b1", null, new ArrayList<Transaction>(), 0); 
		Block third = new Block("b3", "b2", null, new ArrayList<Transaction>(), 0); 
		Block fourth = new Block("b4", "b3", null, new ArrayList<Transaction>(), 0); 
		Block fifth = new Block("b5", "b4", null, new ArrayList<Transaction>(), 0); 
		
		processEvent(null, 0, first);		
		processEvent(null, 0, third);
		processEvent(null, 0, fourth);
		processEvent(null, 0, second);
		processEvent(null, 0, fifth);
		assertEquals(blockchain.size(),5);
	}	
	
	@Test
	public void testForkResolutionWithMissedBlocksPool() {		
		Block b1 = new Block("b1", null, null, new ArrayList<Transaction>(), 0); // only the order of blocks is under test
		Block b2 = new Block("b2", "b1", null, new ArrayList<Transaction>(), 0); 
		Block b3 = new Block("b3", "b1", null, new ArrayList<Transaction>(), 0); 
		Block b4 = new Block("b4", "b3", null, new ArrayList<Transaction>(), 0); 	
		Block b5 = new Block("b5", "b4", null, new ArrayList<Transaction>(), 0); 
		
		processEvent(null, 0, b4);
		processEvent(null, 0, b5);
		processEvent(null, 0, b1);		
		processEvent(null, 0, b2);
		processEvent(null, 0, b3);		
		assertEquals(blockchain.size(),4);
	}		
	
	/** Scans the list of missed blocks trying to find some blocks that can be attached to the head of the blockchain	
	 */
	public void attachMissedBlocks() 
	{		
		Block head = blockchain.get(blockchain.size()-1);
		int i=0;
		while (i < missedBlocks.size()) {
			if (missedBlocks.get(i).getParent() == head.getBid()) {
				head = missedBlocks.remove(i);
				blockchain.add(head);
				i = 0;  			
			}	
			else
				i++;
		}
	}
	
	public void addMissedBlock(Block missed) {
		if (missedBlocks.size() == limit)	
			missedBlocks.removeAll(missedBlocks);
		if (!missedBlocks.contains(missed)) 
			missedBlocks.add(missed);
	}
	
	public void removeTransactionsFromPool(TinyCoinNode tn, Block b) {
		Map<String, Transaction> transPool = tn.getTransPool();
		for (Transaction t : b.getTransactions()) {						
			transPool.remove(t.getTid());
		}
	}
	
	public void processEvent(Node node, int pid, Object event)
	{				
		 if (event instanceof Block) {			
			Block b = (Block)event;	
			String last = blockchain.size()==0 ? null : blockchain.get(blockchain.size()-1).getBid();								
			if ( last == b.getParent() ||
					(fork == true && forked.getBid() == b.getParent())) {
				if (fork == true) {													
					if (forked.getBid() == b.getParent()) {
						Block lastb = blockchain.get(blockchain.size()-1);
						blockchain.remove(lastb); 						
						blockchain.add(forked);					
					}
					fork = false;  
					forked = null;
				}
				blockchain.add(b);
				if (!missedBlocks.isEmpty())
					attachMissedBlocks();										
			}
			else if (blockchain.size() >= 2 && 
					blockchain.get(blockchain.size()-2).getBid() == b.getParent() &&
					blockchain.get(blockchain.size()-1).getBid() != b.getBid() && 
					fork == false) 
			{
					fork = true;
					forked = b;		
					solveForkWithMissedBlocks();
			}
			else if (last != b.getParent())
				addMissedBlock(b);					
		}		
	}
	
	public boolean solveForkWithMissedBlocks() {		
		for (int i=0; i< missedBlocks.size(); i++) {
			if (missedBlocks.get(i).getParent() == forked.getBid()) {	
				Block lastb = blockchain.get(blockchain.size()-1);
				blockchain.remove(lastb); 
				blockchain.add(forked);	
				Block head = missedBlocks.remove(i);
				blockchain.add(head);
				fork = false;
				forked = null;		
				attachMissedBlocks();
				return true;
			}			
		}
		return false;
	}
}
