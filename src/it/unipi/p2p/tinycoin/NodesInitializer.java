package it.unipi.p2p.tinycoin;
import java.util.Random;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class NodesInitializer implements Control
{
	
	private static final String PAR_PMINER = "pminer";
	private static final String PAR_PCPU = "pcpu";
	private static final String PAR_PGPU = "pgpu";
	private static final String PAR_PFPGA = "pfpga";
	private static final String PAR_PASIC = "pasic";
	private static final String PAR_MAX_BALANCE = "max_balance";
	
	// Probability that a netwOrk node is a miner. A node can be either a miner or a TinyCoin normal node
	private double pminer;
	
	// If the node is a miner, then it has different probabilities of mining through CPU, GPU, FPGA or ASIC 
	private double pcpu;
	private double pgpu;
	private double pfpga;
	private double pasic;	
	private double maxBalance;
	

	public NodesInitializer(String prefix)
	{
		pminer = Configuration.getDouble(prefix + "." + PAR_PMINER);
		pcpu = Configuration.getDouble(prefix + "." + PAR_PCPU);
		pgpu = Configuration.getDouble(prefix + "." + PAR_PGPU);
		pfpga = Configuration.getDouble(prefix + "." + PAR_PFPGA);
		pasic = Configuration.getDouble(prefix + "." + PAR_PASIC);
		maxBalance = Configuration.getDouble(prefix + "." + PAR_MAX_BALANCE);
		//TODO: prendi pid dei 2 protocolli dal file di config
	}
	
	/** Initializes the nodes in the network based on the probability values received from Configuration file.
	 *  
	 */
	@Override
	public boolean execute()
	{
		if (pcpu + pgpu + pfpga + pasic != 100) {
			System.err.println("The sum of the probabilities of the mining  HW must be equal to 100");
			return true;		
		}			
			
		TinyCoinNode n = null;
		Random r = new Random(0);
		for (int i=0; i< Network.size(); i++) {
			n = (TinyCoinNode)Network.get(i);
			double b = Math.random()*maxBalance;
			n.setBalance(b);
			//TODO vanno implementati 2 protocolli diversi per Miner e nodo normale. Se un nodo è solo un 
			// nodo normale, allora eseguirà solo il protocollo del nodo; 
			// se è anche un miner, allora eseguirà pure il protocollo del miner
			double drandom = r.nextDouble();
			if (drandom < pminer) { // the node is a miner
				n.setNode(false);
				drandom = r.nextDouble();
				if (drandom < pcpu)
					n.setMtype(MinerType.CPU);
				else if (drandom < pcpu + pgpu)
					n.setMtype(MinerType.GPU);
				else if (drandom < pcpu + pgpu + pfpga)
					n.setMtype(MinerType.FPGA);
				else
					n.setMtype(MinerType.ASIC);	
				//TODO: disable node protocol
			}
			else 
			{
				n.setNode(true);
				n.setMtype(null);
				//TODO: disable miner protocol
			}
			
		}
		
		return false;
	}

}
