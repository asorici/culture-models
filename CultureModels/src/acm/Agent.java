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
		if (ag == null) {
			return;
		}
		
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < nFeatures; i++) {
			if (!features.get(i).equals(ag.features.get(i))) {
				indexes.add(i);
			}
		}

		if (!indexes.isEmpty()) {
			int i = indexes.get(random.nextInt(indexes.size()));
	
			features.set(i, ag.features.get(i));
		}
	}

	public int numberOfMatchingFeatures(Agent<T> ag) {

		int nrMatches = 0;
		for (int i = 0; i < nFeatures; i++) {
			if (features.get(i).equals(ag.features.get(i))) {
				nrMatches++;
			}
		}

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

	public List<T> getFeatures() {
		return features;
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

	@Override
	public int hashCode() {
		String str = toString();
		return str.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Agent<?>)) {
			return false;
		}
		
		final Agent<T> otherAg = (Agent<T>)obj;
		
		if (features.size() != otherAg.getFeatures().size()) {
			return false;
		}
		
		for(int i = 0; i < features.size(); i++) {
			T myFeat = features.get(i);
			T otherFeat = otherAg.getFeatures().get(i);
			
			if (!myFeat.equals(otherFeat)) {
				return false;
			}
		}
		
		return true;
	}
}
