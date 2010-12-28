package gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import acm.Agent;
import acmsim.BaseSimulation;

public class SimulationCanvas extends Canvas {
	private Agent<Integer>[][] agentPopulation;
	
	private static int WIDTH = 300;
	private static int HEIGHT = 300;
	private int CELL_DIM; 
	
	private Graphics bufferGraphics;
	private Image offscreenImage;
	private Image mapImage;
	private Dimension dim;
	
	public SimulationCanvas() {
		CELL_DIM = WIDTH / 10;
		setSize(WIDTH, HEIGHT);
		setLocation(150, 20);
	}
	
	public SimulationCanvas(Agent<Integer>[][] agentPopulation) {
		this.agentPopulation = agentPopulation;
		
		CELL_DIM = WIDTH / agentPopulation.length;
		setSize(WIDTH, HEIGHT);
		setLocation(150, 20);
	}
	
	protected void createMiniMapImage() {
		mapImage = createImage(dim.width, dim.height);
		Graphics g = mapImage.getGraphics();
		if (agentPopulation != null) {
			for (int i = 0; i < agentPopulation.length; i++) {
				for (int j = 0; j < agentPopulation.length; j++) {
					paintGridCell(g, i, j);
				}
			}
		}
		else {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, WIDTH, HEIGHT);
		}
	}
	
    public void paintGridCell(Graphics g, int i, int j) {
		Agent<Integer> ag = agentPopulation[i][j];
		int red = 0, green = 0, blue = 0;

		int groupingSize = (ag.getFeatures().size() + 1) / 3;
		int redGroupSum = 0, greenGroupSum = 0, blueGroupSum = 0;
		int redDomain = 0, greenDomain = 0, blueDomain = 0;

		for (int k = 0; k < groupingSize; k++) {
			redGroupSum = 10 * redGroupSum + ag.getFeatures().get(k);
			redDomain = 10 * redDomain + 9;
		}

		for (int k = groupingSize; k < 2 * groupingSize; k++) {
			greenGroupSum = 10 * greenGroupSum + ag.getFeatures().get(k);
			greenDomain = 10 * greenDomain + 9;
		}

		for (int k = 2 * groupingSize; k < ag.getFeatures().size(); k++) {
			blueGroupSum = 10 * blueGroupSum + ag.getFeatures().get(k);
			blueDomain = 10 * blueDomain + 9;
		}

		red = redGroupSum * 255 / redDomain;
		green = greenGroupSum * 255 / greenDomain;
		blue = blueGroupSum * 255 / blueDomain;

		g.setColor(new Color(red, green, blue));
		g.fillRect(j * CELL_DIM, i * CELL_DIM, CELL_DIM, CELL_DIM);
    }
	
	@Override
	public void update(Graphics g) {
		paint(g);
	}
	
	@Override
	public void paint(Graphics g) {
		
		/* do double buffering */
		if (offscreenImage == null) {
			dim = getSize();
			offscreenImage = createImage(dim.width, dim.height);
			bufferGraphics = offscreenImage.getGraphics();
		}
		
		createMiniMapImage();
		bufferGraphics.clearRect(0, 0, dim.width, dim.height);
		bufferGraphics.drawImage(mapImage, 0, 0, null);
		
		g.drawImage(offscreenImage, 0, 0, null);
	}

	public Agent<Integer>[][] getAgentPopulation() {
		return agentPopulation;
	}

	public void setAgentPopulation(Agent<Integer>[][] agentPopulation) {
		this.agentPopulation = agentPopulation;
		if (agentPopulation != null) {
			CELL_DIM = WIDTH / agentPopulation.length;
		}
	}
}
