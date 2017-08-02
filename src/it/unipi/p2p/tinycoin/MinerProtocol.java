package it.unipi.p2p.tinycoin;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;

//TODO: must implement (also) EDProtocol
public class MinerProtocol implements CDProtocol{

	private int minedBlocks;
	private boolean selected;
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public MinerProtocol(String prefix) {
		minedBlocks = 0;
	}
	
	@Override
	public Object clone() {
		MinerProtocol mp = null;
		try {
			mp = (MinerProtocol)super.clone();
			//mp.setMinedBlocks(this.getMinedBlocks());
		}
		catch(CloneNotSupportedException  e) {
			
		}
		return mp;
	}
	
	public int getMinedBlocks() {
		return minedBlocks;
	}

	public void setMinedBlocks(int minedBlocks) {
		this.minedBlocks = minedBlocks;
	}

	@Override
	public void nextCycle(Node arg0, int arg1) {
		if (isSelected()) {
			setSelected(false);
			minedBlocks++;
			System.out.println("Mined a block!");
			//TODO: send mined blocks to neighbors (use a MESSAGE)
		}
		
	}
	
	//TODO: implement EDProtocol and write a method to manage received messages = blocks 

}
