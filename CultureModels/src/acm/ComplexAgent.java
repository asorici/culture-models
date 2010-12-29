package acm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		ComplexAgent neighbor = (ComplexAgent) ag;
		
		if(neighbor.nFeatures < nFeatures) // no interaction
			return 0;

		int consistencyDiff = Math.abs(exteriorConsistencySum
				- interiorConsistencySum);
		int neighborConsistencyDiff = Math.abs(neighbor
				.getExteriorConsistencySum()
				- neighbor.getInteriorConsistencySum());

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
				features.set(iExt, ag.features.get(iExt));
			}

			if (!intIndexes.isEmpty()) {
				int iInt;
				iInt = selectFeatureToCopy(intIndexes);
				features.set(iInt, ag.features.get(iInt));
			}
		}

		// agent has fewer features than neighbor
		else if (mySize < agSize) {

			ComplexAgent neighbor = (ComplexAgent) ag;

			features.add(splitIndex, neighbor.features
					.get(neighbor.splitIndex - 1));
			features.add(neighbor.features.get(agSize - 1));

		
		} else {
			// do nothing
		}
	}

	// roulette selection of feature
	private int selectFeatureToCopy(List<Integer> indexes) {
		
		int selectedIndex = 0;
		
		List<Double> probabilities = new ArrayList<Double>();

		double s = 0.0;
		for (Integer i : indexes)
			s += (i + 1);
		
		for(Integer i: indexes)
			probabilities.add(((double)(i+1))/s);
			
		double nr = Agent.random.nextDouble();
		
		for(int i=0;i<indexes.size()-1;i++)
		{
			double low = sum(probabilities,i);
			double high = sum(probabilities, i+1);
		
			if(low < nr && nr < high)
			{
				selectedIndex = indexes.get(i);
				break;
			}
		}
		return selectedIndex;

	}

	private double sum(List<Double> prob, int i) {
		double s = 0.0;
		for(Double d: prob)
			s += d;
		
		return s;			
	}

	@Override
	public void update() {
		
		nFeatures = features.size();
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

	public int getSplitIndex() {
		return splitIndex;
	}

}
