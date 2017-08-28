package it.unipi.p2p.tinycoin.controls;
import java.util.Random;

import it.unipi.p2p.tinycoin.MinerType;
import it.unipi.p2p.tinycoin.TinyCoinNode;
import it.unipi.p2p.tinycoin.protocols.MinerProtocol;
import it.unipi.p2p.tinycoin.protocols.SelfishMinerProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class Oracle implements Control {
	
	private static final String PAR_P2 = "prob_2_miners";	
	private static final String PAR_HRCPU = "hr_cpu";
	private static final String PAR_HRGPU = "hr_gpu";
	private static final String PAR_HRFPGA = "hr_fpga";
	private static final String PAR_HRASIC = "hr_asic";
	private static final String PAR_MINER_PROT = "miner_protocol";
	private static final String PAR_SMINER_PROT = "self_miner_protocol";
	
	private final String prefix;
	private final double p2;
	private double pcpu;
	private double pgpu;
	private double pfpga;
	private double pasic;
	private Random r;
	private final int minerPid;
	private final int selfMinerPid;
		
	public Oracle(String prefix) 
	{
		p2 = Configuration.getDouble(prefix + "." + PAR_P2);
		minerPid = Configuration.getPid(prefix + "." + PAR_MINER_PROT);
		selfMinerPid = Configuration.getPid(prefix + "." + PAR_SMINER_PROT);
		pcpu = pgpu = pfpga = pasic = -1.0;	
		this.prefix = prefix;
		r = new Random(0);
	}

	@Override
	public boolean execute() {
	/* Each miner has a given probability of being selected by the oracle. For each type of miner, I define 
	 * the probability of the type as P = total_hash_rate_miner_type / total_hash_rate. So I initialize 
	 * the probabilities the first time that execute() is invoked and not in the constructor, 
	 * because I must be sure that the network has been initialized	*/		
		if (pcpu == -1.0) 
		{
			boolean initSuccess = initializeProb();
			if (!initSuccess)
				return true; 
		}
		
		MinerType m1, m2;
		m1 = getMinerType();       // Always choose one miner
		TinyCoinNode mn1 = (TinyCoinNode)chooseMinerNode(m1);
		if (mn1.isMiner())  
			((MinerProtocol)mn1.getProtocol(minerPid)).setSelected(true);	
		else //selfish miner
			((SelfishMinerProtocol)mn1.getProtocol(selfMinerPid)).setSelected(true);
		double rd = r.nextDouble();
		if (rd < p2) {              // two miners solved PoW concurrently
			m2 = getMinerType();
			TinyCoinNode mn2 = (TinyCoinNode)chooseMinerNode(m2);
			if (mn2.isMiner())
				((MinerProtocol)mn2.getProtocol(minerPid)).setSelected(true); 
			else
				((SelfishMinerProtocol)mn2.getProtocol(selfMinerPid)).setSelected(true); 
		}		
		return false;
	}
	
	private boolean initializeProb() {
		int hrcpu = Configuration.getInt(prefix + "." + PAR_HRCPU);
		int hrgpu = Configuration.getInt(prefix + "." + PAR_HRGPU);
		int hrfpga = Configuration.getInt(prefix + "." + PAR_HRFPGA);
		int hrasic = Configuration.getInt(prefix + "." + PAR_HRASIC);	
		if (hrcpu < 0 || hrgpu < 0 || hrfpga < 0 || hrasic < 0) {
			System.err.println("Hash rates cannot be negative!");
			return false;
		}
		int ncpu, ngpu, nfpga, nasic;
		ncpu = ngpu = nfpga = nasic = 0;
		
		for (int i=0; i< Network.size(); i++) {
			TinyCoinNode n = (TinyCoinNode) Network.get(i);
			if (!n.isNode()) {
				switch(n.getMtype()) {
				case CPU :  ncpu++;
						    break;
				case GPU :  ngpu++;
						    break;
				case FPGA : nfpga++;
				            break;
		        case ASIC : nasic++;
				            break;
				}
			}
		}
		// I get the probabilities of choosing cpu/gpu/fpga/asic miner
		int thr = (ncpu*hrcpu + ngpu*hrgpu + nfpga*hrfpga + nasic*hrasic);
		pcpu = ((double) hrcpu * ncpu) / ((double)  thr);
		pgpu = ((double) hrgpu * ngpu) / ((double)  thr);
		pfpga = ((double) hrfpga * nfpga) / ((double)  thr);
		pasic = ((double) hrasic * nasic) / ((double)  thr);
		return true;
	}
	
	
	private MinerType getMinerType() 
	{
		double rd = r.nextDouble();	
		if (rd < pcpu)
			return MinerType.CPU;
		else if (rd < pcpu + pgpu)
			return MinerType.GPU;
		else if (rd < pcpu + pgpu + pfpga)
			return MinerType.FPGA;
		else
			return MinerType.ASIC;	
	}
	
	/** One miner of the given type is chosen randomly. The randomness is achieved by shuffling
	 *  the nodes in the network and then taking the first miner node with appropriate type.
	 *  @return the miner node which has mined the block
	 */
	private Node chooseMinerNode(MinerType m) {
		Network.shuffle();
		for (int i=0; i< Network.size(); i++) {
			TinyCoinNode n = (TinyCoinNode) Network.get(i);
				if (n.getMtype() == m)
					return n;	
		}
		return null;
	}

}
