package acmsim;
import gui.MainInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.SwingUtilities;

import acm.Agent;

public class BaseSimulation implements Simulation {
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
			
			// randomly select an individual
			int y = Agent.random.nextInt(population.length);
			int x = Agent.random.nextInt(population.length);
			
			final Agent selectedAgent = population[y][x];
			
			// rank agent's neighbors according to their interaction probability
			List<Agent> neighbors = selectedAgent.getNeighbors();
			Collections.sort(neighbors, new Comparator<Agent>() {
			
				@Override
				public int compare(Agent ag1, Agent ag2) {					
					if (selectedAgent.interactionProbability(ag1) == selectedAgent.interactionProbability(ag2)) {
						return 2 * Agent.random.nextInt(2) - 1;
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
									
			Agent selectedNeighbor = neighbors.get(Agent.random.nextInt(neighbors.size()));		
			double interaction = Agent.random.nextDouble();
			
			// pick neighbor to interact with
			for(int i = 0; i < neighbors.size(); i++)
			{
				Agent crtNeighbor = neighbors.get(i);
				double lowThreshold = sum(neighbors,selectedAgent,neighbors.indexOf(crtNeighbor)) / sumAll(neighbors,selectedAgent);
				double highThreshold = sum(neighbors,selectedAgent,neighbors.indexOf(crtNeighbor) + 1)/ sumAll(neighbors, selectedAgent);
			
				if(interaction > lowThreshold && interaction < highThreshold) 
				{
					selectedNeighbor = crtNeighbor;
					break;
				}
			}
			
			
		//	double interactionThreshold = Agent.random.nextDouble();
		//	if (selectedAgent.interactionProbability(selectedNeighbor) > interactionThreshold) {
				selectedAgent.interactWith(selectedNeighbor);			// we might not be able to		
				selectedAgent.update();									// interact with any neighbor
		//	}
		
			
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
	@SuppressWarnings("unchecked")
	@Override
	public int compare(Agent ag1, Agent ag2) {					
		if (selectedAgent.interactionProbability(ag1) == selectedAgent.interactionProbability(ag2)) {
			return 2 * Agent.random.nextInt(2) - 1;
		}
		else {
			if (selectedAgent.interactionProbability(ag1) > selectedAgent.interactionProbability(ag2)) {
				return -1;
			}
			else {
				return 1;
			}
		}
	}
});
*/
