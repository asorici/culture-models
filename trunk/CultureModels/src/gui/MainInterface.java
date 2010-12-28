package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import acm.Agent;
import acm.BalancedAgent;
import acm.SimpleAgent;
import acmsim.BaseSimulation;
import acmsim.Simulation;

public class MainInterface extends JFrame implements ActionListener {
	public static final int WIDTH = 800;
	public static final int HEIGHT = 700;
	
	private Agent<Integer>[][] population;
	private Simulation simulation; 
	
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem populationMenuItem;
	private JMenu agentTypeMenu;
	
	private JLabel agentTypeLabel = new JLabel("        ");
	private JLabel simulationTypeLabel = new JLabel("        ");
	
	private JPanel canvasPanel;
	private JPanel controlPanel;
	private JButton runButton;
	private JTextField nrRunsTextfield;
	
	final JFileChooser fc = new JFileChooser(".");
	
	private SimulationCanvas simCanvas;
	private Boolean simRunning = new Boolean(false);
	
	public MainInterface() {
		simCanvas = new SimulationCanvas();
		initGraphics();
	}
	
	public MainInterface(Agent<Integer>[][] population) {
		this();
		this.population = population;
		this.simCanvas.setAgentPopulation(population);
	}

	private void initGraphics() {
		// frame layout
		setLayout(new BorderLayout(10, 10));
		setSize(WIDTH, HEIGHT);
		
		// init menu
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		populationMenuItem = new JMenuItem("Open population");
		populationMenuItem.addActionListener(this);
		
		// add types of agents
		agentTypeMenu = new JMenu("Select agent");
		JMenuItem simpleAgentItem = new JMenuItem("SimpleAgent");
		simpleAgentItem.addActionListener(this);
		JMenuItem balancedAgentItem = new JMenuItem("BalancedAgent");
		balancedAgentItem.addActionListener(this);
		JMenuItem complexAgentItem = new JMenuItem("ComplexAgent");
		complexAgentItem.addActionListener(this);
		agentTypeMenu.add(simpleAgentItem);
		agentTypeMenu.add(balancedAgentItem);
		agentTypeMenu.add(complexAgentItem);
		
		fileMenu.add(populationMenuItem);
		fileMenu.add(agentTypeMenu);
		
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		
		agentTypeLabel.setSize(new Dimension(60, 20));
		agentTypeLabel.setForeground(Color.WHITE);
		simulationTypeLabel.setSize(new Dimension(60,20));
		simulationTypeLabel.setForeground(Color.WHITE);
		
		// canvas and control panels
		canvasPanel = new JPanel();
		canvasPanel.add(simCanvas);
		
		controlPanel = new JPanel(new GridLayout(3, 0));
		controlPanel.setSize(200, 100);
		controlPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JPanel selectionLabelPanel = new JPanel();
		selectionLabelPanel.add(new JLabel("Ag. Type:"));
		selectionLabelPanel.add(agentTypeLabel);
		selectionLabelPanel.add(new JLabel("Sim. Type:"));
		selectionLabelPanel.add(simulationTypeLabel);
		
		JPanel nrRunsPanel = new JPanel();
		JLabel nrRunsLabel = new JLabel("nrGens:");
		nrRunsTextfield = new JTextField("5000", 10);
		nrRunsTextfield.addActionListener(this);
		nrRunsPanel.add(nrRunsLabel);
		nrRunsPanel.add(nrRunsTextfield);
		
		JPanel buttonPanel = new JPanel();
		runButton = new JButton("Run sim");
		runButton.addActionListener(this);
		buttonPanel.add(runButton);
		
		JPanel runDataPanel = new JPanel();
		runDataPanel.add(nrRunsPanel);
		runDataPanel.add(buttonPanel);
		
		controlPanel.add(selectionLabelPanel);
		controlPanel.add(runDataPanel);
		//controlPanel.add(buttonPanel);
		
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
		//repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(runButton) && simulation != null) {
			synchronized(simRunning) {
				if (!simRunning) {
					int nrGenerations = 5000;
					try {
						nrGenerations = Integer.parseInt(nrRunsTextfield.getText());
					}catch (Exception ex) {
						nrGenerations = 5000;
					}
					
					simRunning = true;
					final int holdNumGenerations = nrGenerations; 
					SwingWorker<Void, Integer> simWorker = new SwingWorker<Void, Integer>() {
						@Override
						protected Void doInBackground() throws Exception {
							try {
								simulation.runSimulation(holdNumGenerations, population);
								return null;
							}
							catch(Exception ex) {
								ex.printStackTrace();
								return null;
							}
							
						}
						
						@Override
						protected void done() {
							synchronized (simRunning) {
								simRunning = false;
							}
						}
						
					};
					simWorker.execute();
					
				}
			}
			
		}
		
		if (evt.getSource().equals(populationMenuItem)) {
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            String filename = fc.getSelectedFile().getAbsolutePath();
	            setPopulation(loadPopulation(filename, BalancedAgent.class));
	            agentTypeLabel.setText("Simple Agent");
				simulationTypeLabel.setText("Base Sim");
				agentTypeLabel.repaint();
				simulationTypeLabel.repaint();
			}
		}
		
		if (evt.getActionCommand().equalsIgnoreCase("simpleagent")) {
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            String filename = fc.getSelectedFile().getAbsolutePath();
				setPopulation(loadPopulation(filename, BalancedAgent.class));
				agentTypeLabel.setText("Simple Agent");
				simulationTypeLabel.setText("Base Sim");
				agentTypeLabel.repaint();
				simulationTypeLabel.repaint();
			}
		}
		
		if (evt.getActionCommand().equalsIgnoreCase("balancedagent")) {
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            String filename = fc.getSelectedFile().getAbsolutePath();
				setPopulation(loadPopulation(filename, BalancedAgent.class));
				agentTypeLabel.setText("Balanced Agent");
				simulationTypeLabel.setText("Base Sim");
				agentTypeLabel.repaint();
				simulationTypeLabel.repaint();
			}
		}
		
		if (evt.getActionCommand().equalsIgnoreCase("complexagent")) {
			
		}
	}

	public Agent<Integer>[][] getPopulation() {
		return population;
	}

	public void setPopulation(Agent<Integer>[][] population) {
		this.population = population;
		if (population != null) {
			simulation = new BaseSimulation(this);
		}
		else {
			simulation = null;
		}
		
		simCanvas.setAgentPopulation(population);
		simCanvas.repaint();
	}

	public Boolean getSimRunning() {
		return simRunning;
	}

	public void setSimRunning(Boolean simRunning) {
		this.simRunning = simRunning;
	}
	
	public static Agent<Integer>[][] loadPopulation(String filename, Class<?extends Agent<Integer>> agentClass) {
		Agent<Integer>[][] population = null;
		BufferedReader br;
		
		try {
			br = new BufferedReader(new FileReader(filename));
			int n = Integer.parseInt(br.readLine().trim());
			int f = Integer.parseInt(br.readLine().trim());

			//population = new SimpleAgent[n][n];
			population = new BalancedAgent[n][n];
			
			for (int i = 0; i < n; i++) {
				String[] individuals = br.readLine().split(" ");
				
				for (int j = 0; j < n; j++) {
					
					population[i][j] = agentClass.getConstructor(new Class[] {int.class, String.class}).newInstance(new Object[] {f, individuals[j]});
					
					//population[i][j] = new SimpleAgent(f, individuals[j]);
					//population[i][j] = new BalancedAgent(f, individuals[j]);
					population[i][j].setPosX(j);
					population[i][j].setPosY(i);
				}
				
				if (population == null) {
					break;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
			population = null;
		}
		
		return population;
	}
	
	public static void main(String[] args) {
		
		//Agent[][] pop = loadPopulation("files/population1.txt");
		//mainInterface = new MainInterface(pop);
		final MainInterface mainInterface = new MainInterface();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainInterface.setVisible(true);
			}
		});
		
		//runSimulation(5000, pop);
	}
}
