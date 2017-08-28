package it.unipi.p2p.tinycoin;

public class Transaction {
	
	private final String tid;
	private final TinyCoinNode input;
	private final TinyCoinNode output;
	private final double amount;
	private final double fee;
	
	public Transaction(String id, TinyCoinNode input, TinyCoinNode output, double amount,
			double fee) {
		tid = id;
		this.input = input;
		this.output = output;
		this.amount = amount;
		this.fee = fee;
	}
	
	public double getAmount() {
		return amount;
	}

	public TinyCoinNode getOutput() {
		return output;
	}

	public double getFee() {
		return fee;
	}

	public String getTid() {
		return tid;
	}
	
	@Override
	public String toString() {
		return "Transaction " + tid + ": Source = " + input.getID() + ", Destination = " +
				output.getID() + ", amount = " + amount + ", fee = " + fee;
	}

}
