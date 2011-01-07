package acm;

public class BalancedAgent extends Agent<Integer> {
	private int splitIndex;
	private int exteriorConsistencySum = 0;
	private int interiorConsistencySum = 0;
	
	public BalancedAgent(int nFeatures, String featureString) {
		super(nFeatures, featureString);
		splitIndex = (nFeatures + 1) / 2;
		
		for (int i = 0; i < featureString.length(); i++) {
			features.add(Integer.valueOf(featureString.substring(i, i + 1)));
		}
		
		update();
	}

	@Override
	public double interactionProbability(Agent<Integer> ag) {
		BalancedAgent neighbor = (BalancedAgent)ag;
		
		int consistencyDiff = Math.abs(exteriorConsistencySum - interiorConsistencySum);
		int neighborConsistencyDiff = Math.abs(neighbor.getExteriorConsistencySum() - neighbor.getInteriorConsistencySum());
		
		if (neighborConsistencyDiff < consistencyDiff) {
			return consistencyDiff - neighborConsistencyDiff;
		}
		
		return 0;
	}

	@Override
	public void update() {
		exteriorConsistencySum = 0;
		interiorConsistencySum = 0;
	
		for (int i = 0; i < splitIndex; i++) {
			int featVal = features.get(i);
			exteriorConsistencySum += featVal;
		}
		
		for (int i = splitIndex; i < nFeatures; i++) {
			int featVal = features.get(i);
			interiorConsistencySum += featVal;
		}
	}

	public int getExteriorConsistencySum() {
		return exteriorConsistencySum;
	}

	public void setExteriorConsistencySum(int exteriorConsistencySum) {
		this.exteriorConsistencySum = exteriorConsistencySum;
	}

	public int getInteriorConsistencySum() {
		return interiorConsistencySum;
	}

	public void setInteriorConsistencySum(int interiorConsistencySum) {
		this.interiorConsistencySum = interiorConsistencySum;
	}

	@Override
	public int getSplitIndex() {
		return splitIndex;
	}
}
