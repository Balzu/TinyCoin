package it.unipi.p2p.tinycoin.initializer;
import java.util.Random;

import it.unipi.p2p.tinycoin.MinerType;
import it.unipi.p2p.tinycoin.NodeType;
import it.unipi.p2p.tinycoin.TinyCoinNode;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class NodesInitializer implements Control
{
	
	private static final String PAR_PMINER = "pminer";
	private static final String PAR_PSMINER = "p_self_miner";
	private static final String PAR_PCPU = "pcpu";
	private static final String PAR_PGPU = "pgpu";
	private static final String PAR_PFPGA = "pfpga";
	private static final String PAR_PASIC = "pasic";
	private static final String PAR_MAX_BALANCE = "max_balance";
	
	// Probability that a network node is a miner.
	private double pminer;
	private double psminer;
	
	// If the node is a miner, then it has different probabilities of mining through CPU, GPU, FPGA or ASIC 
	private double pcpu;
	private double pgpu;
	private double pfpga;
	private double pasic;	
	private double maxBalance;
	

	public NodesInitializer(String prefix)
	{
		pminer = Configuration.getDouble(prefix + "." + PAR_PMINER);
		psminer = pminer = Configuration.getDouble(prefix + "." + PAR_PSMINER);
		pcpu = Configuration.getDouble(prefix + "." + PAR_PCPU);
		pgpu = Configuration.getDouble(prefix + "." + PAR_PGPU);
		pfpga = Configuration.getDouble(prefix + "." + PAR_PFPGA);
		pasic = Configuration.getDouble(prefix + "." + PAR_PASIC);
		maxBalance = Configuration.getDouble(prefix + "." + PAR_MAX_BALANCE);
	}
	
	/** Initializes the nodes in the network based on the probability values received from Configuration file.
	 */
	@Override
	public boolean execute()
	{
		if (pcpu + pgpu + pfpga + pasic != 1) {
			System.err.println("The sum of the probabilities of the mining  HW must be equal to 1");
			return true;		
		}			
			
		TinyCoinNode n = null;
		Random r = new Random(0);
		for (int i=0; i< Network.size(); i++) {
			n = (TinyCoinNode)Network.get(i);
			double b = Math.random()*maxBalance;
			n.setBalance(b);
			double drandom = r.nextDouble();
			if (drandom < pminer) { // the node is a miner
				drandom = r.nextDouble();				
				if (drandom < psminer) //Node is a selfish miner
					n.setNodetype(NodeType.SELFISH_MINER);
				else
					n.setNodetype(NodeType.MINER);				
				drandom = r.nextDouble();
				if (drandom < pcpu)
					n.setMtype(MinerType.CPU);
				else if (drandom < pcpu + pgpu)
					n.setMtype(MinerType.GPU);
				else if (drandom < pcpu + pgpu + pfpga)
					n.setMtype(MinerType.FPGA);
				else
					n.setMtype(MinerType.ASIC);	
			}
			else 
			{
				n.setNodetype(NodeType.NODE);
				n.setMtype(null);
			}			
		}		
		return false;
	}

}
