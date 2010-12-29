package acmsim;

import java.util.HashMap;
import java.util.List;

import acm.Agent;
import acm.ComplexAgent;

public class ComplexSimulation implements Simulation {

	@Override
	public List<Agent> getNeighbors(Agent<?> ag, Agent<?>[][] pop) {
		return null;
	}

	@Override
	public void runSimulation(int numGenerations, Agent<?>[][] population) {

	}

	public HashMap<Integer, Integer> regionsPerFeatureValue(Agent<Integer>[][] population, int featureIndex) {
		HashMap<Integer, Integer> regionsPerFeatureVal = new HashMap<Integer, Integer>();
		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population.length; j++) {
				if (population[i][j].getFeatures().size() > featureIndex) {
					int featureVal = population[i][j].getFeatures().get(featureIndex);
					Integer existingCount = regionsPerFeatureVal.get(featureVal);
					if (existingCount == null) {
						regionsPerFeatureVal.put(featureVal, 1);
					}
					else {
						regionsPerFeatureVal.put(featureVal, existingCount + 1);
					}
				}
			}
		}
		
		return regionsPerFeatureVal;
	}
	
	// globalHomogeneityMeasure - nrOfRegions even if unstable
	public HashMap<Agent<Integer>, Integer> globalHomogeneityMeasure(Agent<Integer>[][] population) {
		HashMap<Agent<Integer>, Integer> homogeneousRegions = new HashMap<Agent<Integer>, Integer>();
		
		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population[i].length; j++) {
				Agent<Integer> ag = population[i][j];
				
				Integer regionMembers = homogeneousRegions.get(ag);
				if (regionMembers == null) {
					homogeneousRegions.put(ag, 1);
				}
				else {
					homogeneousRegions.put(ag, regionMembers + 1);
				}
			}
		}
		
		return homogeneousRegions;
	}
	
	// localHomogeneityMeasure - number of pairs that had any differences (in this case inconsistency)
	public int[] localHomogeneityMeasure(ComplexAgent[][] population, int nrFeatures, int splitIndex) {
		int[] nrNeighborDiffs = new int[nrFeatures];
		for (int k = 0; k < nrNeighborDiffs.length; k++) {
			nrNeighborDiffs[k] = 0;
		}
		
		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population[i].length; j++) {
				ComplexAgent ag = population[i][j];
				List<Agent> neighbors = ag.getNeighbors();
				
				for (Agent neighbor : neighbors) {
					ComplexAgent neighborAgent = (ComplexAgent)neighbor;
					
					// if the agents have different feature sizes
					if (ag.getFeatures().size() != neighborAgent.getFeatures().size()) {
						ComplexAgent minAgent = ag;
						ComplexAgent otherAgent = neighborAgent;
						if (neighborAgent.getFeatures().size() < ag.getFeatures().size()) {
							minAgent = neighborAgent;
							otherAgent = ag;
						}
						
						// compare up to splitIndex
						for (int k = 0; k < minAgent.getSplitIndex(); k++) {
							if (!minAgent.getFeatures().get(k).equals(otherAgent.getFeatures().get(k))) {
								nrNeighborDiffs[k]++;
							}
						}
						
						// compare from splitIndex onwards
						for (int k = minAgent.getSplitIndex(); k < minAgent.getFeatures().size(); k++) {
							int otherIndex = k - minAgent.getSplitIndex() + otherAgent.getSplitIndex();
							
							if (!minAgent.getFeatures().get(k).equals(otherAgent.getFeatures().get(otherIndex))) {
								nrNeighborDiffs[k - minAgent.getSplitIndex() + splitIndex]++;
							}
						}
					}
					else {			// otherwise they have equal feature sizes
						if (ag.getFeatures().size() == nrFeatures) {
							for (int k = 0; k < nrFeatures; k++) {
								if (!ag.getFeatures().get(k).equals(neighborAgent.getFeatures().get(k))) {
									nrNeighborDiffs[k]++;
								}
							}
						}
						else {		// but the size might be different than the expected one (the maximum)
							// compare up to splitIndex
							for (int k = 0; k < ag.getSplitIndex(); k++) {
								if (!ag.getFeatures().get(k).equals(neighborAgent.getFeatures().get(k))) {
									nrNeighborDiffs[k]++;
								}
							}
							
							// compare from splitIndex onwards
							for (int k = ag.getSplitIndex(); k < ag.getFeatures().size(); k++) {
								if (!ag.getFeatures().get(k).equals(neighborAgent.getFeatures().get(k))) {
									nrNeighborDiffs[k - ag.getSplitIndex() + splitIndex]++;
								}
							}
						}
					}
				}
			}
		}
		
		for (int k = 0; k < nrNeighborDiffs.length; k++) {
			nrNeighborDiffs[k] /= 8;		// account for duplicates from all neighbors
		}
		
		return nrNeighborDiffs;
	}
}
