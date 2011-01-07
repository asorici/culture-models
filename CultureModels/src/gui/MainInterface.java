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
import java.lang.reflect.Array;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

import acm.Agent;
import acm.BalancedAgent;
import acm.ComplexAgent;
import acm.SimpleAgent;
import acmsim.BaseSimulation;
import acmsim.ComplexSimulation;
import acmsim.Simulation;

@SuppressWarnings("serial")
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
	private JLabel currentGenLabel = new JLabel("0");
	
	private JTextArea simProgressDisplayArea;
	
	final JFileChooser fc = new JFileChooser(".");
	
	private SimulationCanvas simCanvas;
	private Boolean simRunning = new Boolean(false);
	
	// stat graphs data
	private JFrame chartFrame;
	JFreeChart globalHomChart;
	JFreeChart localHomChart;
	Dataset[] regionsPerFeatureVal;
	DefaultPieDataset globalHomogeneityMeasure;
	DefaultCategoryDataset localHomogeneityMeasure;
	
	public MainInterface() {
		simCanvas = new SimulationCanvas();
		initGraphics();
		initChartGraphics();
	}

	public MainInterface(Agent<Integer>[][] population) {
		this();
		this.population = population;
		this.simCanvas.setAgentPopulation(population);
	}

	private void initChartGraphics() {
		chartFrame = new JFrame("Simulation stats");
		chartFrame.setLayout(new GridLayout(2, 1));
		chartFrame.setSize(900, 600);
		
		globalHomogeneityMeasure = new DefaultPieDataset();
		localHomogeneityMeasure = new DefaultCategoryDataset();
		
		globalHomChart = ChartFactory.createPieChart("Global Homogeneity Measure", globalHomogeneityMeasure, true, true, true);
		localHomChart = ChartFactory.createLineChart(
				"Local Homogeneity Measure", "gen. iter.", "no. of different pairs per feature",
				localHomogeneityMeasure, PlotOrientation.VERTICAL, true, false, false);
		
		JPanel globalHomChartPanel = new ChartPanel(globalHomChart);
		JPanel localHomChartPanel = new ChartPanel(localHomChart);
		
		chartFrame.add(globalHomChartPanel);
		chartFrame.add(localHomChartPanel);
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
		
		simProgressDisplayArea = new JTextArea();
		simProgressDisplayArea.setLocation(100, 20);
		simProgressDisplayArea.setPreferredSize(new Dimension(400, 300));
		simProgressDisplayArea.setEditable(false);
		simProgressDisplayArea.setText("");
		JScrollPane progressDisplayPanel = new JScrollPane(simProgressDisplayArea);
		//progressDisplayPanel.setSize(400, 300);
		
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
		
		JPanel currentGenPanel = new JPanel();
		JLabel genInfoLabel = new JLabel("currentGen:");
		currentGenPanel.add(genInfoLabel);
		currentGenPanel.add(currentGenLabel);
		
		controlPanel.add(selectionLabelPanel);
		controlPanel.add(runDataPanel);
		
		add(BorderLayout.CENTER, canvasPanel);
		add(BorderLayout.EAST, controlPanel);
		add(BorderLayout.SOUTH, progressDisplayPanel);
		
		addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent we) {
                System.exit(0);
            }
        });
        
		setTitle("ACM Simulation");
	}

	public void updateCanvas() {
		//Simulations.printPopulation(population);
		simProgressDisplayArea.setText(printPopulation(population));
		simProgressDisplayArea.repaint();
		simCanvas.repaint();
		currentGenLabel.setText("" + simulation.gen);
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
			synchronized(simRunning) {
				if (!simRunning) {
					int returnVal = fc.showOpenDialog(this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            String filename = fc.getSelectedFile().getAbsolutePath();
			            setPopulation(loadPopulation(filename, SimpleAgent.class));
			            agentTypeLabel.setText("Simple Agent");
						simulationTypeLabel.setText("Base Sim");
						agentTypeLabel.repaint();
						simulationTypeLabel.repaint();
					}
				}
			}
		}
		
		if (evt.getActionCommand().equalsIgnoreCase("simpleagent")) {
			synchronized(simRunning) {
				if (!simRunning) {
					int returnVal = fc.showOpenDialog(this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            String filename = fc.getSelectedFile().getAbsolutePath();
						setPopulation(loadPopulation(filename, SimpleAgent.class));
						agentTypeLabel.setText("Simple Agent");
						simulationTypeLabel.setText("Base Sim");
						agentTypeLabel.repaint();
						simulationTypeLabel.repaint();
					}
				}
			}
		}
		
		if (evt.getActionCommand().equalsIgnoreCase("balancedagent")) {
			synchronized(simRunning) {
				if (!simRunning) {
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
			}
		}
		
		if (evt.getActionCommand().equalsIgnoreCase("complexagent")) {
			synchronized(simRunning) {
				if (!simRunning) {
					int returnVal = fc.showOpenDialog(this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            String filename = fc.getSelectedFile().getAbsolutePath();
						
			            population = loadPopulation(filename, ComplexAgent.class);
			            if (population != null) {
			            	simulation = new ComplexSimulation(this);
			            }
			            else {
			            	simulation = null;
			            }
						
			            simProgressDisplayArea.setText(printPopulation(population));
			    		simProgressDisplayArea.repaint();
			    		
			    		simCanvas.setAgentPopulation(population);
			    		simCanvas.repaint();
			            
						agentTypeLabel.setText("Complex Agent");
						simulationTypeLabel.setText("Complex Sim");
						agentTypeLabel.repaint();
						simulationTypeLabel.repaint();
					}
				}
			}
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
		
		simProgressDisplayArea.setText(printPopulation(population));
		simProgressDisplayArea.repaint();
		
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
			//population = new BalancedAgent[n][n];
			population = (Agent<Integer>[][]) Array.newInstance(agentClass, new int[] {n, n});
			//population = new Agent<Integer>[n][n];
			
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
	
	private String printPopulation(Agent<Integer>[][] population) {
		String info = "";
		if (population != null) {
			for (int i = 0; i < population.length; i++) {
				for (int j = 0; j < population.length; j++) {
					info += population[i][j].toString() + "        ";
				}
				info += "\n";
			}
			
			info += "\n";
		}
		
		return info;
	}
	
	public static void main(String[] args) {
		
		final MainInterface mainInterface = new MainInterface();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainInterface.setVisible(true);
				mainInterface.getChartFrame().setVisible(true);
			}
		});
	}

	public void setChartFrame(JFrame chartFrame) {
		this.chartFrame = chartFrame;
	}

	public JFrame getChartFrame() {
		return chartFrame;
	}

	public void updateRegsPerFeatureGraph(HashMap<Integer, Integer> regsPerFeatureVal, int k) {
		
	}

	public void updateGlobalHomogeneityGraph(HashMap<Agent<Integer>, Integer> globalHomogeneityMap) {
		globalHomogeneityMeasure.clear();
		
		for (Agent<Integer> ag : globalHomogeneityMap.keySet()) {
			globalHomogeneityMeasure.setValue(ag.toString(), globalHomogeneityMap.get(ag));
		}
		
		chartFrame.repaint();
	}

	public void updateLocalHomogeneityGraph(int[] localHomogeneityMap, int genIter) {
		for (int k = 0; k < localHomogeneityMap.length; k++) {
			localHomogeneityMeasure.addValue((Number)localHomogeneityMap[k], "feature[" + k + "]", genIter);
		}
		
		//chartFrame.repaint();
	}
}
