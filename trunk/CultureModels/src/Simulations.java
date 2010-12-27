import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import acm.Agent;
import acm.SimpleAgent;

public class Simulations {

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

	public static List<Agent> getNeighbors(Agent<?> ag, Agent<?>[][] pop) {

		List<Agent> neighbors = new ArrayList<Agent>();

		int i = ag.getPosY();
		int j = ag.getPosX();

		int size = pop.length;

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

		return neighbors;
	}

	public static void runSimulation(int numGenerations, Agent<?>[][] population) {

		for (int i = 0; i < population.length; i++)
			for (int j = 0; j < population.length; j++) {
				population[i][j].setNeighbors(getNeighbors(population[i][j], population));
			}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Agent[][] pop = loadPopulation("files/population1.txt");

		System.out.println(pop[0][0] + "\n" + pop[0][1]);

		System.out.println(pop[0][0].numberOfMatchingFeatures(pop[0][1]));

		pop[0][0].interactWith(pop[0][1]);

		System.out.println(pop[0][0] + " vs. " + pop[0][1]);

		System.out.println(pop[0][0].numberOfMatchingFeatures(pop[0][1]));

	}

}
