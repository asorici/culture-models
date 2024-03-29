package acm;

import java.util.ArrayList;
import java.util.List;

public class ComplexAgent extends Agent<Integer> {

	public static final int MAX_FEATURES = 9;
	static final double HIGH_FEATURE_PROB = 0.9;
	static final double LOW_FEATURE_PROB = 0.1;

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
		ComplexAgent neighbor = (ComplexAgent) ag;

		if (neighbor.nFeatures < nFeatures) // no interaction
			return 0;

		int consistencyDiff = Math.abs(exteriorConsistencySum - interiorConsistencySum);
		int neighborConsistencyDiff = Math.abs(neighbor.getExteriorConsistencySum() - neighbor.getInteriorConsistencySum());

		if (neighborConsistencyDiff < consistencyDiff) {
			return consistencyDiff - neighborConsistencyDiff;
		}

		return 0;
	}

	@Override
	public void interactWith(Agent<Integer> ag) {

		if (ag == null) {
			return;
		}

		int mySize = features.size();
		int agSize = ag.features.size();

		if (mySize == agSize) {

			ArrayList<Integer> extIndexes = new ArrayList<Integer>();
			ArrayList<Integer> intIndexes = new ArrayList<Integer>();

			for (int i = 0; i < splitIndex; i++)
				if (!features.get(i).equals(ag.features.get(i)))
					extIndexes.add(i);

			for (int i = splitIndex; i < mySize; i++)
				if (!features.get(i).equals(ag.features.get(i)))
					intIndexes.add(i);

			if (!extIndexes.isEmpty()) {
				int iExt;
				iExt = selectFeatureToCopy(extIndexes);
				
				double threshold = Agent.random.nextDouble();
				//if ((iExt + 1) / sum(extIndexes) > threshold)
				//System.out.println(getChangeProbability(extIndexes, iExt) + " vs " + threshold);
				if (getChangeProbability(extIndexes, iExt) > threshold) {
					features.set(iExt, ag.features.get(iExt));
				}
			}
			
			if (!intIndexes.isEmpty()) {
				int iInt;
				iInt = selectFeatureToCopy(intIndexes);
				
				double threshold = Agent.random.nextDouble();
				//if (iInt / sum(intIndexes) > threshold)
				if (getChangeProbability(intIndexes, iInt) > threshold) {
					features.set(iInt, ag.features.get(iInt));
				}
			}
		}

		// agent has fewer features than neighbor
		else if (mySize < agSize) {

			ComplexAgent neighbor = (ComplexAgent) ag;

			//features.add(splitIndex, neighbor.features.get(neighbor.splitIndex - 1));
			features.add(splitIndex, Agent.random.nextInt(10));
			
			//features.add(neighbor.features.get(agSize - 1));
			int adoptedInternalValue = (neighbor.features.get(agSize - 1) + (2 * Agent.random.nextInt(3) - 2) + 10) % 10;
			features.add(adoptedInternalValue);
			
			nFeatures = features.size();
			splitIndex++;
		} else {
			// do nothing
		}
	}

	private int sum(ArrayList<Integer> extIndexes) {
		int s = 0;
		for (Integer i : extIndexes)
			s += (i + 1);
		return s;
	}

	// roulette selection of feature
	private int selectFeatureToCopy(List<Integer> indexes) {

		int selectedIndex = 0;

		List<Double> probabilities = new ArrayList<Double>();

		double s = 0.0;
		for (Integer i : indexes)
			s += (i + 1);

		for (Integer i : indexes)
			probabilities.add(((double) (i + 1)) / s);

		double nr = Agent.random.nextDouble();

		for (int i = 0; i < indexes.size(); i++) {
			double low = probSum(probabilities, i);
			double high = probSum(probabilities, i + 1);

			if (low <= nr && nr < high) {
				selectedIndex = indexes.get(i);
				break;
			}
		}
		
		return selectedIndex;

	}

	private double probSum(List<Double> prob, int i) {
		double s = 0.0;
		for (int k = 0; k < i; k++)
			s += prob.get(k);

		return s;
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

	public int getSplitIndex() {
		return splitIndex;
	}

	public void technologicalChange(int value) {

		if (nFeatures < MAX_FEATURES) {
			int extValue = Agent.random.nextInt(10);
			int intValue = value;

			features.add(splitIndex, extValue);
			features.add(intValue);

			nFeatures = features.size();
			splitIndex++;
		}
	}
	
	private double getChangeProbability(List<Integer> indexes, int selIndex) {
		int size = indexes.size();
		if (size == 1) {
			return HIGH_FEATURE_PROB;
		}
		
		double ration = (HIGH_FEATURE_PROB - LOW_FEATURE_PROB) / (double) (size - 1);
		int minIndex = indexes.get(0);
		for (int i = 1; i < size; i++) {
			if (indexes.get(i) < minIndex) {
				minIndex = indexes.get(i);
			}
		}
		
		int progressionIndex = selIndex - minIndex;
		
		return LOW_FEATURE_PROB + progressionIndex * ration;
	}

}
