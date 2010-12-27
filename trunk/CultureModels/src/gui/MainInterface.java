package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import acm.Agent;

public class MainInterface extends JFrame implements ActionListener {
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	
	private Agent[][] population;
	
	private JMenu menu;
	private JPanel canvasPanel;
	private JPanel controlPanel;
	
	SimulationCanvas simCanvas;
	
	public MainInterface(Agent[][] population) {
		this.population = population;
		simCanvas = new SimulationCanvas(population);
		
		initGraphics();
	}

	private void initGraphics() {
		// frame layout
		setLayout(new BorderLayout(10, 10));
		setSize(WIDTH, HEIGHT);
		
		// init menu
		menu = new JMenu();
		JMenuItem fileMenuItem = new JMenuItem("File");
		menu.add(fileMenuItem);
		
		// canvas and control panels
		
	}

	
	@Override
	public void actionPerformed(ActionEvent evt) {
		
	}
}
