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
	static final int CHANGE_PERIOD = 200;

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

		//neighbors.add(pop[(i - 1 + size) % size][(j - 1 + size) % size]);
		neighbors.add(pop[(i - 1 + size) % size][j]);
		///neighbors.add(pop[(i - 1 + size) % size][(j + 1 + size) % size]);
		neighbors.add(pop[i][(j - 1 + size) % size]);
		neighbors.add(pop[i][(j + 1 + size) % size]);
		//neighbors.add(pop[(i + 1 + size) % size][(j - 1 + size) % size]);
		neighbors.add(pop[(i + 1 + size) % size][j]);
		//neighbors.add(pop[(i + 1 + size) % size][(j + 1 + size) % size]);

		return neighbors;
	}

	public static void changeRegion(Agent<?>[][] population, int corner, int value) {
		int iL = 0, iH = REGION, jL = 0, jH = REGION;
		switch (corner % 4) {
		case 0:
			break;
		case 1:
			iL = 0;
			iH = REGION;
			jL = population.length - REGION;
			jH = population.length;
			break;
		case 2:
			iL = population.length - REGION;
			iH = population.length;
			jL = 0;
			jH = REGION;			
			break;
		case 3:
			iL = population.length - REGION;
			iH = population.length;
			jL = population.length - REGION;
			jH = population.length;
			break;
		}
		
		for(int i=iL;i<iH;i++)
			for(int j=jL;j<jH;j++)								
				((ComplexAgent)population[i][j]).technologicalChange(value);
	}
	
	@Override
	public void runSimulation(int numGenerations, Agent<?>[][] population) {

		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population.length; j++) {
				population[i][j].setNeighbors(getNeighbors(population[i][j], population));
			}
		}
		
		noChangeCt = 0;
		gen = 0;
		while (gen < numGenerations) {
			//System.out.println("gen: " + gen);

			if (gen >= CHANGE_PERIOD && gen % CHANGE_PERIOD == 0) {
				
				// select individual for technological change
				int corner = Agent.random.nextInt(4);
				int xc = 1, yc = 1;
				
				int value = Agent.random.nextInt(10);
				ComplexSimulation.changeRegion(population,corner,value);			
				
			}
			
			// global homogeneity measure
			final HashMap<Agent<Integer>, Integer> globalHomogeneityMap = globalHomogeneityMeasure((Agent<Integer>[][])population);
			if (gen == 0) {
				prevStableRegionCount = globalHomogeneityMap.keySet().size();
				prevLocalHomogeneityMeasure = localHomogeneityMeasure((Agent<Integer>[][])population, ComplexAgent.MAX_FEATURES, (ComplexAgent.MAX_FEATURES + 1) / 2);
			}
			currentStableRegionCount = globalHomogeneityMap.keySet().size();
			
			// // localHomogeneity measure
			localHomogeneityMeasure = localHomogeneityMeasure((Agent<Integer>[][])population, ComplexAgent.MAX_FEATURES, (ComplexAgent.MAX_FEATURES + 1) / 2);
			
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
			
			for (int a = 0; a < numInteractingAgents; a++)
			{
				// randomly select an individual for interaction
				int y = Agent.random.nextInt(population.length);
				int x = Agent.random.nextInt(population.length);
				
				final Agent selectedAgent = population[y][x];
				
				// rank agent's neighbors according to their interaction probability
				List<Agent> neighbors = selectedAgent.getNeighbors();
				
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
			}
			
			final Agent<?>[][] holdPopulation = population;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
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
		
		// regions per feature value
		for (int k = 0; k < population[0][0].getFeatures().size(); k++) {
			HashMap<Integer, Integer> regsPerFeatureVal = regionsPerFeatureValue((Agent<Integer>[][])population, k);
			System.out.println("======== Region count per feature[" + k + "] ========");
			for (Integer featVal : regsPerFeatureVal.keySet()) {
				System.out.println("feature val " + featVal + ": " + regsPerFeatureVal.get(featVal));
			}
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

	

	
}
