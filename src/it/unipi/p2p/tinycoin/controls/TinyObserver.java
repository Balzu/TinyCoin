package it.unipi.p2p.tinycoin.controls;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import it.unipi.p2p.tinycoin.Block;
import it.unipi.p2p.tinycoin.TinyCoinNode;
import it.unipi.p2p.tinycoin.protocols.NodeProtocol;
import it.unipi.p2p.tinycoin.protocols.SelfishMinerProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class TinyObserver implements Control{
	
	private static final String PAR_NODE_PROT = "node_protocol";
	private static final String PAR_MINER_PROT = "miner_protocol";
	private static final String PAR_SMINER_PROT = "selfish_miner_protocol";
	private static final String PAR_REPETITION = "repetition";
	private static final String PAR_SMINER = "p_self_miner";
	private static final String PAR_HRCPU = "hr_cpu";
	private static final String PAR_HRGPU = "hr_gpu";
	private static final String PAR_HRFPGA = "hr_fpga";
	private static final String PAR_HRASIC = "hr_asic";
	private static final String PAR_ONLYLATENCY = "only_latency";
	private static final String PAR_DELAY = "delay";
	
	private final int npid;
	private final int mpid;
	private final int smpid;
	private final int repetition;
	private final double psm;
	private final int hrcpu;
	private final int hrgpu;
	private final int hrfpga;
	private final int hrasic;
	private final boolean onlyLatency;
	private final int delay;
	
	private int cycle;
	private TinyCoinNode node;
	private final String prefix;
	
	public TinyObserver(String prefix) {
		npid = Configuration.getPid(prefix + "." + PAR_NODE_PROT);
		mpid = Configuration.getPid(prefix + "." + PAR_MINER_PROT);
		smpid = Configuration.getPid(prefix + "." + PAR_SMINER_PROT);
		repetition = Configuration.getInt(prefix + "." + PAR_REPETITION);
		psm = Configuration.getDouble(prefix + "." + PAR_SMINER);
		hrcpu = Configuration.getInt(prefix + "." + PAR_HRCPU);
		hrgpu = Configuration.getInt(prefix + "." + PAR_HRGPU);
		hrfpga = Configuration.getInt(prefix + "." + PAR_HRFPGA);
		hrasic = Configuration.getInt(prefix + "." + PAR_HRASIC);	
		onlyLatency = Configuration.getBoolean(prefix + "." + PAR_ONLYLATENCY); 
		delay = Configuration.getInt(prefix + "." + PAR_DELAY);
		cycle = 0;
		this.prefix = prefix;
	}

	@Override
	public boolean execute()
	{					
	    int forks=0;
		int sminers=0;
		FileWriter forkStats = null;
		FileWriter blockchainStats = null;
		FileWriter latencyStats = null;
		BufferedWriter bw = null;			
		cycle++;
			
		try 
		{	
			if (cycle == 1)  // Initialization
			{  	
				for (int i =0; i< Network.size(); i++) {
					if (((TinyCoinNode)Network.get(i)).isSelfishMiner()) {
						node = (TinyCoinNode)Network.get(i); 
						break;
					}					
				}				
				if (onlyLatency == true)
				{
					latencyStats = new FileWriter("docs/statistics/latency_R" + repetition + 
							"_D" + delay + ".dat", false);
					bw = new BufferedWriter(latencyStats);
					bw.write("# Mined_Blocks" + " "  + "Cycle \n");				
					bw.close();	
				}
				else 
				{					
					forkStats = new FileWriter("docs/statistics/forks_R" + repetition +
							"_P" + psm + ".dat", false);
					bw = new BufferedWriter(forkStats);
					bw.write("# Forks_number" + " " + "Cycle \n");	
					bw.close();
					
					blockchainStats = new FileWriter("docs/statistics/blockchain_R" + repetition + 
							"_P" + psm + ".dat", false);
					bw = new BufferedWriter(blockchainStats);
					bw.write("# Honest_blocks" + " " + "Fraudolent_blocks" + " " + "Cycle \n");				
					bw.close();				
					
								
					int hrsminers = 0;
					int hrhonests = 0;
					TinyCoinNode n = null;
					for (int i=0; i< Network.size(); i++) {
						n = (TinyCoinNode)Network.get(i);					
						if (n.isSelfishMiner()) 
							hrsminers += getHashRate(n);
						else if (n.isMiner()) 					
							hrhonests += getHashRate(n);							
					}
					blockchainStats = new FileWriter("docs/statistics/hashrate_R" + repetition + 
							"_P" + psm + ".dat", false);
					bw = new BufferedWriter(blockchainStats);
					bw.write("# Honest_HR" + " " + "Fraudolent_HR" + " " + "Probability(SelfishMiner) \n");
					bw.write(hrhonests + "            " + hrsminers + "            " + psm);
					bw.close();
				}					
			}			
			
			TinyCoinNode n = null;
			for (int i=0; i< Network.size(); i++) {
				n = (TinyCoinNode)Network.get(i);				
				if (n.isSelfishMiner()) 
					sminers++;				
				else 
					forks+=((NodeProtocol)n.getProtocol(npid)).getNumForks();						
			}
			
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
			// Add the fraudolent blocks that are in the private blockchain but not yet in the
			// public one, if any. This is an optimistic assumption, indeed they could never end up in the blockchain
			List<Block> privateBlockchain = ((SelfishMinerProtocol)node.getProtocol(smpid)).getPrivateBlockchain();
			if (privateBlockchain.size() > blockchain.size())
				fraudolentBlocks += privateBlockchain.size() - blockchain.size();
			
			if (onlyLatency == true) {
				int totalBlocks = honestBlocks + fraudolentBlocks;
				latencyStats = new FileWriter("docs/statistics/latency_R" + repetition + 
						"_D" + delay + ".dat", true);
				bw = new BufferedWriter(latencyStats);
				bw.write(totalBlocks + "            "  + cycle + "\n");
				bw.close();
			}			
			else {
				blockchainStats = new FileWriter("docs/statistics/blockchain_R" + repetition + 
						"_P" + psm + ".dat", true);
				bw = new BufferedWriter(blockchainStats);
				bw.write(honestBlocks + "            " + 
						fraudolentBlocks + "            "  + cycle + "\n");			
				bw.close();
				
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
				forkStats = new FileWriter("docs/statistics/forks_R" + repetition + 
						"_P" + psm + ".dat", true);
				bw = new BufferedWriter(forkStats);
				bw.write(forks + "            " + cycle + "\n");
				bw.close();
			}			
		}
		catch (IOException e) {
			System.err.println(e);
		}				
		return false;
	}
	
	
	public int getHashRate(TinyCoinNode n) {
		switch (n.getMtype()) {
			case CPU: return hrcpu;
			case GPU: return hrgpu;
			case FPGA: return hrfpga;
			case ASIC: return hrasic;
			default: return 0;
		}
	}

}
