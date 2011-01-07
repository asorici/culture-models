package acmsim;

import gui.MainInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import acm.Agent;
import acm.ComplexAgent;

public class ComplexSimulation extends Simulation {

	private static final double NEW_FEATURE_ATTRACTION_THRESHOLD = 0.8;
	private static final int REGION = 3;
	static final int CHANGE_PERIOD = 2000;

	private MainInterface mainInterface;

	public ComplexSimulation(MainInterface mainInterface) {
		this.mainInterface = mainInterface;
	}

	@Override
	public List<Agent> getNeighbors(Agent<?> ag, Agent<?>[][] pop) {
		List<Agent> neighbors = new ArrayList<Agent>();

		int i = ag.getPosY();
		int j = ag.getPosX();

		int size = pop.length;

		neighbors.add(pop[(i - 1 + size) % size][(j - 1 + size) % size]);
		neighbors.add(pop[(i - 1 + size) % size][j]);
		neighbors.add(pop[(i - 1 + size) % size][(j + 1 + size) % size]);
		neighbors.add(pop[i][(j - 1 + size) % size]);
		neighbors.add(pop[i][(j + 1 + size) % size]);
		neighbors.add(pop[(i + 1 + size) % size][(j - 1 + size) % size]);
		neighbors.add(pop[(i + 1 + size) % size][j]);
		neighbors.add(pop[(i + 1 + size) % size][(j + 1 + size) % size]);

		return neighbors;
	}

	@Override
	public void runSimulation(int numGenerations, Agent<?>[][] population) {

		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population.length; j++) {
				population[i][j].setNeighbors(getNeighbors(population[i][j], population));
			}
		}

		int gen = 0;
		while (gen < numGenerations) {
			System.out.println("gen: " + gen);

			if (gen >= CHANGE_PERIOD && gen % CHANGE_PERIOD == 0) {
				
				// select individual for technological change
				int corner = Agent.random.nextInt(4);
				int xc = 1, yc = 1;
				
				switch (corner % 4) {
					case 0: // upper left
						xc = Agent.random.nextInt(REGION);
						yc = Agent.random.nextInt(REGION);
						break;
					case 1: // lower left
						xc = Agent.random.nextInt(REGION);
						yc = population.length - 1 - Agent.random.nextInt(REGION);
						break;
					case 2: // upper right
						xc = population.length - 1 - Agent.random.nextInt(REGION);
						yc = Agent.random.nextInt(REGION);
						break;
					case 3: // lower right
						xc = population.length - 1 - Agent.random.nextInt(REGION);
						yc = population.length - 1 - Agent.random.nextInt(REGION);
						break;
					default:
						break;
				}
				
				ComplexAgent changingAgent = (ComplexAgent)population[yc][xc];
				changingAgent.technologicalChange();
			}

			// take homogeneity measures
			if (gen % 100 == 0) {
				// regions per feature value
				for (int k = 0; k < population[0][0].getFeatures().size(); k++) {
					HashMap<Integer, Integer> regsPerFeatureVal = regionsPerFeatureValue((Agent<Integer>[][])population, k);
					mainInterface.updateRegsPerFeatureGraph(regsPerFeatureVal, k);
				}
				
				// global homogeneity measure
				HashMap<Agent<Integer>, Integer> globalHomogeneityMap = globalHomogeneityMeasure((Agent<Integer>[][])population);
				mainInterface.updateGlobalHomogeneityGraph(globalHomogeneityMap);
				
				// localHomogeneity measure
				int[] localHomogeneityMeasure = localHomogeneityMeasure((ComplexAgent[][])population, ComplexAgent.MAX_FEATURES, (ComplexAgent.MAX_FEATURES + 1) / 2);
				mainInterface.updateLocalHomogeneityGraph(localHomogeneityMeasure, gen);
			}
			
			// randomly select an individual for interaction
			int y = Agent.random.nextInt(population.length);
			int x = Agent.random.nextInt(population.length);
			
			final Agent selectedAgent = population[y][x];
			
			// rank agent's neighbors according to their interaction probability
			List<Agent> neighbors = selectedAgent.getNeighbors();
			
			/*
			System.out.println(neighbors);			
			for (int k = 0; k < neighbors.size(); k++) {
				Agent ag = neighbors.get(k);
				System.out.printf("%6.2f ", selectedAgent.interactionProbability(ag));
			}
			System.out.println();
			*/
			
			double interactionSelection = Agent.random.nextDouble();

			// pick neighbor to interact with
			Agent selectedNeighbor = null;
			
			List<Agent> bigNeighbors = new ArrayList<Agent>();
			for(Agent n:neighbors) {
				if(n.getFeatures().size() > selectedAgent.getFeatures().size()) {
					bigNeighbors.add(n);
				}
			}
			
			if (((ComplexAgent)selectedAgent).getExteriorConsistencySum() == ((ComplexAgent)selectedAgent).getInteriorConsistencySum()) {
				if (!bigNeighbors.isEmpty()) {
					selectedNeighbor = bigNeighbors.get(Agent.random.nextInt(bigNeighbors.size()));
					//System.out.println(selectedNeighbor);
					selectedAgent.interactWith(selectedNeighbor); 	// we might not be able to
					selectedAgent.update(); 						// interact with any neighbor
				}
			}
			else {
				double nr = Agent.random.nextDouble();
				if(nr < NEW_FEATURE_ATTRACTION_THRESHOLD  && !bigNeighbors.isEmpty()) {		
					selectedNeighbor = bigNeighbors.get(Agent.random.nextInt(bigNeighbors.size()));
				}
				else{
					
					for (int i = 0; i < neighbors.size(); i++) 
					{
						Agent crtNeighbor = neighbors.get(i);
						double lowThreshold = sum(neighbors, selectedAgent, neighbors.indexOf(crtNeighbor)) / sumAll(neighbors, selectedAgent);
						double highThreshold = sum(neighbors, selectedAgent, neighbors.indexOf(crtNeighbor) + 1) / sumAll(neighbors, selectedAgent);
						
						if (interactionSelection >= lowThreshold && interactionSelection < highThreshold) 
						{
							selectedNeighbor = crtNeighbor;
							break;
						}
					}			
				}
				
				double interactionThreshold = Agent.random.nextDouble();
				if (selectedNeighbor != null && (selectedAgent.interactionProbability(selectedNeighbor) > interactionThreshold)) {
					//System.out.println(selectedNeighbor);
					selectedAgent.interactWith(selectedNeighbor); 	// we might not be able to
					selectedAgent.update(); 						// interact with any neighbor
				} else {
					//System.out.println("null");
				}
			}
			
			final Agent<?>[][] holdPopulation = population;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// mainInterface.setPopulation(holdPopulation);
					mainInterface.updateCanvas();
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
	}

	@SuppressWarnings("unchecked")
	public static double sum(List<Agent> neighbors, Agent selectedAgent,
			int index) {
		double sum = 0.0;
		for (int i = 0; i < index; i++) {
			sum += selectedAgent.interactionProbability(neighbors.get(i));
		}

		return sum;
	}

	@SuppressWarnings("unchecked")
	public static double sumAll(List<Agent> neighbors, Agent selectedAgent) {
		double sum = 0.0;
		for (int i = 0; i < neighbors.size(); i++) {
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

	public HashMap<Integer, Integer> regionsPerFeatureValue(Agent<Integer>[][] population, int featureIndex) {
		HashMap<Integer, Integer> regionsPerFeatureVal = new HashMap<Integer, Integer>();
		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population.length; j++) {
				if (population[i][j].getFeatures().size() > featureIndex) {
					int featureVal = population[i][j].getFeatures().get(featureIndex);
					Integer existingCount = regionsPerFeatureVal.get(featureVal);
					if (existingCount == null) {
						regionsPerFeatureVal.put(featureVal, 1);
					} else {
						regionsPerFeatureVal.put(featureVal, existingCount + 1);
					}
				}
			}
		}

		return regionsPerFeatureVal;
	}

	
}