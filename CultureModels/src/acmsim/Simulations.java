package acmsim;
import gui.MainInterface;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.SwingUtilities;

import acm.Agent;
import acm.SimpleAgent;

public class Simulations {
	private static MainInterface mainInterface;
	
	public static Agent<?>[][] loadPopulation(String filename) {
		Agent<?>[][] population = null;
		BufferedReader br;
		
		try {
			br = new BufferedReader(new FileReader(filename));
			int n = Integer.parseInt(br.readLine().trim());
			int f = Integer.parseInt(br.readLine().trim());

			population = new SimpleAgent[n][n];

			for (int i = 0; i < n; i++) {
				String[] individuals = br.readLine().split(" ");
				
				for (int j = 0; j < n; j++) {
					population[i][j] = new SimpleAgent(f, individuals[j]);
					population[i][j].setPosX(j);
					population[i][j].setPosY(i);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return population;
	}

	@SuppressWarnings("unchecked")
	public static List<Agent> getNeighbors(Agent<?> ag, Agent<?>[][] pop) {

		List<Agent> neighbors = new ArrayList<Agent>();

		int i = ag.getPosY();
		int j = ag.getPosX();

		int size = pop.length;

		
		neighbors.add(pop[(i-1) % size][(j - 1) % size]);
		neighbors.add(pop[(i-1) % size][j]);
		neighbors.add(pop[(i-1) % size][(j + 1) % size]);
		neighbors.add(pop[i][(j - 1) % size]);
		neighbors.add(pop[i][(j + 1) % size]);
		neighbors.add(pop[(i+1) % size][(j - 1) % size]);
		neighbors.add(pop[(i+1) % size][j]);
		neighbors.add(pop[(i+1) % size][(j + 1) % size]);
		
		
		/*
		if (i > 0 && j > 0)
			neighbors.add(pop[i - 1][j - 1]);
		if (i > 0)
			neighbors.add(pop[i - 1][j]);
		if (i > 0 && j < size - 1)
			neighbors.add(pop[i - 1][j + 1]);
		if (j > 0)
			neighbors.add(pop[i][j - 1]);
		if (j < size - 1)
			neighbors.add(pop[i][j + 1]);
		if (i < size - 1 && j > 0)
			neighbors.add(pop[i + 1][j - 1]);
		if (i < size - 1)
			neighbors.add(pop[i + 1][j]);
		if (i < size - 1 && j < size - 1)
			neighbors.add(pop[i + 1][j + 1]);
		*/
		
		return neighbors;
	}

	public static void runSimulation(int numGenerations, Agent<Integer>[][] population) {

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
		
			
			final Agent<Integer>[][] holdPopulation = population;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					//mainInterface.setPopulation(holdPopulation);
					mainInterface.updateCanvas();
				}
			});
			
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			gen++;
		}
		
		printPopulation(population);
	}

	@SuppressWarnings("unchecked")
	public static double sum(List<Agent> neighbors, Agent selectedAgent, int index){
		double sum = 0.0;
		for(int i = 0;i < index; i++)
			sum += selectedAgent.interactionProbability(neighbors.get(i));
		
		return sum;
	}
	
	@SuppressWarnings("unchecked")
	public static double sumAll(List<Agent> neighbors, Agent selectedAgent){
		double sum = 0.0;
		for(int i = 0;i < neighbors.size(); i++)
			sum += selectedAgent.interactionProbability(neighbors.get(i));
		
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
	
	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Agent[][] pop = loadPopulation("files/population1.txt");
		mainInterface = new MainInterface(pop);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainInterface.setVisible(true);
			}
		});
		
		//runSimulation(5000, pop);
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
