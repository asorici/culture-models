package acmsim;

import java.util.List;

import acm.Agent;

public interface Simulation {
	public static final int DELAY = 5;
	
	@SuppressWarnings("unchecked")
	public List<Agent> getNeighbors(Agent<?> ag, Agent<?>[][] pop);

	public void runSimulation(int numGenerations, Agent<?>[][] population);

}