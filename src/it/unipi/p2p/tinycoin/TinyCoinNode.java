package it.unipi.p2p.tinycoin;
import peersim.core.GeneralNode;

public class TinyCoinNode extends GeneralNode{
	
	private MinerType mtype;
	private boolean node;	
	private double balance;
	

	public MinerType getMtype() {
		return mtype;
	}

	public void setMtype(MinerType mtype) {
		this.mtype = mtype;
	}

	public boolean isNode() {
		return node;
	}
	
	public boolean isMiner() {
		return !isNode();
	}

	public void setNode(boolean node) {
		this.node = node;
	}
	

	public TinyCoinNode(String prefix) {
		super(prefix);
	}
	
	
	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	@Override
	public Object clone()
	{
		TinyCoinNode clone = (TinyCoinNode)super.clone();
		//TODO: devo lasciarli i 2 metodi sotto o non servono?
		clone.setMtype(this.getMtype());
		clone.setNode(this.isNode());
		return clone;
	}

}
