package acm;


public class SimpleAgent extends Agent<Integer> {

	public SimpleAgent(int nFeatures, String featureStr) {
		super(nFeatures, featureStr);

		for (int i = 0; i < featureStr.length(); i++) {
			features.add(Integer.valueOf(featureStr.substring(i, i + 1)));
		}
	}

	@Override
	public double interactionProbability(Agent<Integer> ag) {
		return (double)numberOfMatchingFeatures(ag) / (double)nFeatures;
	}

	
}
