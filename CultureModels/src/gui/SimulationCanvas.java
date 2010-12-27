package gui;

import java.awt.Canvas;

import acm.Agent;

public class SimulationCanvas extends Canvas {
	private Agent[][] agentPopulation;
	
	public SimulationCanvas(Agent[][] agentPopulation) {
		this.agentPopulation = agentPopulation;
	}
}
