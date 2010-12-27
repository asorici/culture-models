package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

import acm.Agent;
import acmsim.Simulations;

public class MainInterface extends JFrame implements ActionListener {
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 700;
	
	private Agent<Integer>[][] population;
	
	private JMenu menu;
	private JPanel canvasPanel;
	private JPanel controlPanel;
	private JButton runButton;
	private JTextField nrRunsTextfield;
	
	SimulationCanvas simCanvas;
	
	public MainInterface(Agent<Integer>[][] population) {
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
		canvasPanel = new JPanel();
		canvasPanel.add(simCanvas);
		
		controlPanel = new JPanel();
		JPanel nrRunsPanel = new JPanel();
		JLabel nrRunsLabel = new JLabel("nrGens:");
		
		nrRunsTextfield = new JTextField(10);
		nrRunsTextfield.addActionListener(this);
		
		nrRunsPanel.add(nrRunsLabel);
		nrRunsPanel.add(nrRunsTextfield);
		
		JPanel buttonPanel = new JPanel();
		runButton = new JButton("Run sim");
		runButton.addActionListener(this);
		buttonPanel.add(runButton);
		
		controlPanel.add(nrRunsPanel);
		controlPanel.add(buttonPanel);
		
		add(BorderLayout.NORTH, menu);
		add(BorderLayout.CENTER, canvasPanel);
		add(BorderLayout.EAST, controlPanel);
		
		addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent we) {
                System.exit(0);
            }
        });
        
		setTitle("ACM Simulation");
	}

	public void updateCanvas() {
		//Simulations.printPopulation(population);
		simCanvas.repaint();
		repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		
	}

	public Agent<Integer>[][] getPopulation() {
		return population;
	}

	public void setPopulation(Agent<Integer>[][] population) {
		this.population = population;
		simCanvas.setAgentPopulation(population);
	}
}
