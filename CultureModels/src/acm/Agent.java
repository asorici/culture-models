package acm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Agent<T> {

	public static Random random = new Random();

	int nFeatures;
	List<T> features;
	List<Agent> neighbors;

	// position in the population (matrix)
	int posX, posY;

	@SuppressWarnings("unchecked")
	public Agent(int nFeatures, String featureString) {
		super();
		this.nFeatures = nFeatures;
		this.features = new ArrayList<T>();
		this.neighbors = new ArrayList<Agent>();
	}

	public void interactWith(Agent<T> ag) {

		ArrayList<Integer> indexes = new ArrayList<Integer>();

		for (int i = 0; i < nFeatures; i++)
			if (!features.get(i).equals(ag.features.get(i)))
				indexes.add(i);

		//System.out.println("Non matching: " + indexes);

		if (!indexes.isEmpty()) {
			int i = indexes.get(random.nextInt(indexes.size()));
			//System.out.println("i = " + i);
			//System.out.println(features + " <>" + ag.features);
	
			features.set(i, ag.features.get(i));
		}
	}

	public int numberOfMatchingFeatures(Agent<T> ag) {

		int nrMatches = 0;
		for (int i = 0; i < nFeatures; i++)
			if (features.get(i).equals(ag.features.get(i)))
				nrMatches++;

		return nrMatches;
	}

	public abstract double interactionProbability(Agent<T> ag);

	public void update() {
	}
	
	@SuppressWarnings("unchecked")
	public List<Agent> getNeighbors() {
		return neighbors;
	}
	
	@SuppressWarnings("unchecked")
	public void setNeighbors(List<Agent> neighbors) {
		this.neighbors = neighbors;
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public String toString() {
		String info = "";
		for (T feat : features) {
			info += feat.toString();
		}
		
		return info;
	}
}
