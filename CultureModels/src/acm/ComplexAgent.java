package acm;

import java.util.ArrayList;

public class ComplexAgent extends Agent<Integer> {
	private int splitIndex;
	private int exteriorConsistencySum = 0;
	private int interiorConsistencySum = 0;
	
	
	public ComplexAgent(int nFeatures, String featureString) {
		super(nFeatures, featureString);
		splitIndex = 2 * nFeatures / 3;
		
		for (int i = 0; i < featureString.length(); i++) {
			features.add(Integer.valueOf(featureString.substring(i, i + 1)));
		}
		
		update();
	}

	@Override
	public double interactionProbability(Agent<Integer> ag) {
		ComplexAgent neighbor = (ComplexAgent)ag;
		
		int consistencyDiff = Math.abs(exteriorConsistencySum - interiorConsistencySum);
		int neighborConsistencyDiff = Math.abs(neighbor.getExteriorConsistencySum() - neighbor.getInteriorConsistencySum());
		
		if (neighborConsistencyDiff < consistencyDiff) {
			return consistencyDiff - neighborConsistencyDiff;
		}
		
		return 0;
		
	}
	
	@Override 
	public void interactWith(Agent<Integer> ag) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < nFeatures; i++) {
			if (!features.get(i).equals(ag.features.get(i))) {
				indexes.add(i);
			}
		}

		if (!indexes.isEmpty()) {
			int i = indexes.get(random.nextInt(indexes.size()));
	
			features.set(i, ag.features.get(i));
		}
	}
	
	@Override
	public void update() {
		splitIndex = 2 * nFeatures / 3;
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
}
