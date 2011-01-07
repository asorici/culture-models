package acmsim;
import gui.MainInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import acm.Agent;
import acm.ComplexAgent;

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
		
		neighbors.add(pop[(i - 1 + size) % size][(j - 1 + size) % size]);
		neighbors.add(pop[(i - 1  + size) % size][j]);
		neighbors.add(pop[(i - 1  + size) % size][(j + 1 + size) % size]);
		neighbors.add(pop[i][(j - 1  + size) % size]);
		neighbors.add(pop[i][(j + 1 + size) % size]);
		neighbors.add(pop[(i + 1 + size) % size][(j - 1 + size) % size]);
		neighbors.add(pop[(i + 1  + size) % size][j]);
		neighbors.add(pop[(i + 1  + size) % size][(j + 1 + size) % size]);
		
		return neighbors;
	}

	public void runSimulation(int numGenerations, Agent<?>[][] population) {

		for (int i = 0; i < population.length; i++) {
			for (int j = 0; j < population.length; j++) {
				population[i][j].setNeighbors(getNeighbors(population[i][j], population));
			}
		}
		
		int gen = 0;
		while (gen < numGenerations) {
			System.out.println("gen: " + gen);
			
			// take homogeneity measures
			if (gen % 100 == 0) {
				// global homogeneity measure
				HashMap<Agent<Integer>, Integer> globalHomogeneityMap = globalHomogeneityMeasure((Agent<Integer>[][])population);
				mainInterface.updateGlobalHomogeneityGraph(globalHomogeneityMap);
				
				// localHomogeneity measure
				int[] localHomogeneityMeasure = localHomogeneityMeasure((ComplexAgent[][])population, ComplexAgent.MAX_FEATURES, (ComplexAgent.MAX_FEATURES + 1) / 2);
				mainInterface.updateLocalHomogeneityGraph(localHomogeneityMeasure, gen);
			}
			
			// randomly select an individual
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
			
			/*
			if (selectedNeighbor != null) {
				System.out.println(selectedNeighbor);
			}
			else {
				System.out.println("null");
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
		
			final Agent<?>[][] holdPopulation = population;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					//mainInterface.setPopulation(holdPopulation);
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

/*
Collections.sort(neighbors, new Comparator<Agent>() {			
	@Override
	public int compare(Agent ag1, Agent ag2) {					
		if (selectedAgent.interactionProbability(ag1) == selectedAgent.interactionProbability(ag2)) {
			return 0;
		}
		else {
			if (selectedAgent.interactionProbability(ag1) > selectedAgent.interactionProbability(ag2)) {
				return 1;
			}
			else {
				return -1;
			}
		}
	}
});
*/
