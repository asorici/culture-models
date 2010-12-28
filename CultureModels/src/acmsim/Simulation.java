package acmsim;

import java.util.List;

import acm.Agent;

public interface Simulation {

	@SuppressWarnings("unchecked")
	public List<Agent> getNeighbors(Agent<?> ag, Agent<?>[][] pop);

	public void runSimulation(int numGenerations, Agent<?>[][] population);

}