package acmsim;
import gui.MainInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import acm.Agent;

public class BaseSimulation extends Simulation {
	private MainInterface mainInterface;
	
	public BaseSimulation(MainInterface mainInterface) {
		this.mainInterface = mainInterface;
	}
	
	@SuppressWarnings("unchecked")
	public List<Agent> getNeighbors(Agent<?> ag, Agent<?>[][] pop) {

		List<Agent> neighbors = new ArrayList<Agent>();

		int i = ag.getPosY();
		int j = ag.getPosX();

		int size = pop.length;
		
		//neighbors.add(pop[(i - 1 + size) % size][(j - 1 + size) % size]);
		neighbors.add(pop[(i - 1  + size) % size][j]);
		//neighbors.add(pop[(i - 1  + size) % size][(j + 1 + size) % size]);
		neighbors.add(pop[i][(j - 1  + size) % size]);
		neighbors.add(pop[i][(j + 1 + size) % size]);
		//neighbors.add(pop[(i + 1 + size) % size][(j - 1 + size) % size]);
		neighbors.add(pop[(i + 1  + size) % size][j]);
		//neighbors.add(pop[(i + 1  + size) % size][(j + 1 + size) % size]);
		
		return neighbors;
	}

	public void runSimulation(int numGenerations, Agent<?>[][] population) {

		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population.length; j++) {
				population[i][j].setNeighbors(getNeighbors(population[i][j], population));
			}
		}
		
		gen = 0;
		while (gen < numGenerations) {
			//System.out.println("gen: " + gen);
			
			// global homogeneity measure
			final HashMap<Agent<Integer>, Integer> globalHomogeneityMap = globalHomogeneityMeasure((Agent<Integer>[][])population);
			if (gen == 0) {
				prevStableRegionCount = globalHomogeneityMap.keySet().size();
				prevLocalHomogeneityMeasure = localHomogeneityMeasure((Agent<Integer>[][])population, population[0][0].getFeatures().size(), population[0][0].getSplitIndex());
			}
			currentStableRegionCount = globalHomogeneityMap.keySet().size();
			
			// // localHomogeneity measure
			localHomogeneityMeasure = localHomogeneityMeasure((Agent<Integer>[][])population, population[0][0].getFeatures().size(), population[0][0].getSplitIndex());
			
			if (gen != 0) {
				boolean noChange = true;
				for (int k = 0; k < prevLocalHomogeneityMeasure.length; k++) {
					if (localHomogeneityMeasure[k] != prevLocalHomogeneityMeasure[k]) {
						noChange = false;
					}
				}
				
				if (noChange) {
					noChangeCt++;
					if (noChangeCt >= noChangeThreshold) {
						System.out.println("###############################");
						System.out.println("# Convergence reached already #");
						System.out.println("###############################");
						System.out.println("NR_GENS: " + (gen + mainInterface.globalGenerationCount));
						
						break;
					}
				}
				else {
					noChangeCt = 0;
					for (int k = 0; k < prevLocalHomogeneityMeasure.length; k++) {
						prevLocalHomogeneityMeasure[k] = localHomogeneityMeasure[k];
					}
				}
			}
			
			// display homogeneity measures
			if (gen % 100 == 0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						mainInterface.updateGlobalHomogeneityGraph(globalHomogeneityMap);
						mainInterface.updateLocalHomogeneityGraph(localHomogeneityMeasure, gen);
					}
				});
			}
			
			int numInteractingAgents = Simulation.PERCENT_CHANGE * population.length * population.length / 100;
			if (prevStableRegionCount != 0) {
				if (currentStableRegionCount <= prevStableRegionCount / 2) {
					reductionFactor = reductionFactor * 3 / 2;
					prevStableRegionCount = currentStableRegionCount;
				}
				
				numInteractingAgents /= reductionFactor;
				if (numInteractingAgents < 1) {
					numInteractingAgents = 1;
				}
			}
			
			
			for(int a = 0; a < numInteractingAgents; a++)
			{
				// randomly select an individual
				int y = Agent.random.nextInt(population.length);
				int x = Agent.random.nextInt(population.length);
				
				final Agent selectedAgent = population[y][x];
				
				// rank agent's neighbors according to their interaction probability
				List<Agent> neighbors = selectedAgent.getNeighbors();
				
				double interactionSelection = Agent.random.nextDouble();
				
				// pick neighbor to interact with
				Agent selectedNeighbor = neighbors.get(Agent.random.nextInt(neighbors.size()));
				/*
				Agent selectedNeighbor = null;
				for (int i = 0; i < neighbors.size(); i++)
				{
					Agent crtNeighbor = neighbors.get(i);
					double lowThreshold = sum(neighbors, selectedAgent, neighbors.indexOf(crtNeighbor)) / sumAll(neighbors,selectedAgent);
					double highThreshold = sum(neighbors, selectedAgent, neighbors.indexOf(crtNeighbor) + 1) / sumAll(neighbors, selectedAgent);
					
					if (interactionSelection >= lowThreshold && interactionSelection < highThreshold) 
					{
						selectedNeighbor = crtNeighbor;
						break;
					}
				}
				*/
				
				double interactionThreshold = Agent.random.nextDouble();
				if (selectedNeighbor != null && selectedAgent.interactionProbability(selectedNeighbor) > interactionThreshold) {
					//System.out.println(selectedNeighbor);
					selectedAgent.interactWith(selectedNeighbor);			// we might not be able to		
					selectedAgent.update();									// interact with any neighbor
				}
				else {
					//System.out.println("null");
				}
			}
			
			final Agent<?>[][] holdPopulation = population;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					//if (gen % 200 == 0) {
						mainInterface.updateCanvas();
					//}
				}
			});
			
			try {
				Thread.sleep(Simulation.DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			gen++;
		}
		
		printPopulation(population);
		
		// regions per feature value
		for (int k = 0; k < population[0][0].getFeatures().size(); k++) {
			HashMap<Integer, Integer> regsPerFeatureVal = regionsPerFeatureValue((Agent<Integer>[][])population, k);
			System.out.println("======== Region count per feature[" + k + "] ========");
			for (Integer featVal : regsPerFeatureVal.keySet()) {
				System.out.println("feature val " + featVal + ": " + regsPerFeatureVal.get(featVal));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static double sum(List<Agent> neighbors, Agent selectedAgent, int index) {
		double sum = 0.0;
		for(int i = 0; i < index; i++) {
			sum += selectedAgent.interactionProbability(neighbors.get(i));
		}
		
		return sum;
	}
	
	@SuppressWarnings("unchecked")
	public static double sumAll(List<Agent> neighbors, Agent selectedAgent){
		double sum = 0.0;
		for(int i = 0; i < neighbors.size(); i++) {
			sum += selectedAgent.interactionProbability(neighbors.get(i));
		}
		return sum;
	}
	
	public static void printPopulation(Agent<?>[][] population) {
		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population.length; j++) {
				System.out.print(population[i][j].toString() + "  ");
			}
			System.out.println();
		}
		
		System.out.println();
	}

}

