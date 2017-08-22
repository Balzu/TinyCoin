package it.unipi.p2p.tinycoin.controls;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import it.unipi.p2p.tinycoin.Block;
import it.unipi.p2p.tinycoin.TinyCoinNode;
import it.unipi.p2p.tinycoin.protocols.NodeProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class TinyObserver implements Control{
	
	private static final String PAR_NODE_PROT = "node_protocol";
	private static final String PAR_MINER_PROT = "miner_protocol";
	private static final String PAR_SMINER_PROT = "selfish_miner_protocol";
	private static final String PAR_REPETITION = "repetition";
	
	private final int npid;
	private final int mpid;
	private final int smpid;
	private final int repetition;
	
	private int cycle;
	private TinyCoinNode node;
	
	
	public TinyObserver(String prefix) {
		npid = Configuration.getPid(prefix + "." + PAR_NODE_PROT);
		mpid = Configuration.getPid(prefix + "." + PAR_MINER_PROT);
		smpid = Configuration.getPid(prefix + "." + PAR_SMINER_PROT);
		repetition = Configuration.getInt(prefix + "." + PAR_REPETITION);
		cycle = 0;
	}

	@Override
	public boolean execute()
	{		
		
			
			int forks=0;
			int sminers=0;
			FileWriter forkStats = null;
			FileWriter blockchainStats = null;
			BufferedWriter bw = null;

			
			cycle++;
			
			try 
			{
			
			if (cycle == 1) 
			{  // clean stat files and initialize	
				node = (TinyCoinNode)Network.get(0);    // To be sure to always consider the blockchain of the same node
				
				forkStats = new FileWriter("docs/statistics/forks_R" + repetition + ".dat", false);
				bw = new BufferedWriter(forkStats);
				bw.write("# Forks_number" + " " + "Cycle \n");	
				bw.close();
				
				blockchainStats = new FileWriter("docs/statistics/blockchain_R" + repetition + ".dat", false);
				bw = new BufferedWriter(blockchainStats);
				bw.write("# Honest_blocks" + " " + "Fraudolent_blocks" + " " + "Cycle \n");				
				bw.close();
			}
			
			
			
			TinyCoinNode n = null;
			for (int i=0; i< Network.size(); i++) {
				n = (TinyCoinNode)Network.get(i);
				
				if (n.isSelfishMiner()) 
				{
					//TODO stuff in case of selfish miner
					sminers++;
				}
				else 
				{
					forks+=((NodeProtocol)n.getProtocol(npid)).getNumForks();
				}			
			}
			
			// Statistics about forks
			int honests = Network.size()- sminers;
			System.out.println("Honest nodes and miners are " + honests);
			try {
				forks = forks / honests;  // take the avg
				}
			catch(ArithmeticException e) {
				forks = ((NodeProtocol)node.getProtocol(npid)).getNumForks();
			}
			System.out.println("Forks are " + forks + " at cycle " + cycle);
			forkStats = new FileWriter("docs/statistics/forks_R" + repetition + ".dat", true);
			bw = new BufferedWriter(forkStats);
			bw.write(forks + "            " + cycle + "\n");
			bw.close();
			
			//Statistics about blockchain
			int honestBlocks = 0;
			int fraudolentBlocks = 0;			
			List<Block> blockchain = node.getBlockchain();
			for (Block b : blockchain) {
				if (b.getMiner().isSelfishMiner())
					fraudolentBlocks++;
				else
					honestBlocks++;
			}
			blockchainStats = new FileWriter("docs/statistics/blockchain_R" + repetition + ".dat", true);
			bw = new BufferedWriter(blockchainStats);
			bw.write(honestBlocks + "            " + 
					fraudolentBlocks + "            "  + cycle + "\n");
			
			
			bw.close();
		}
		catch (IOException e) {
			
		}		
		
		return false;
	}

}
