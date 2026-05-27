package parkinson.learning;

import org.apache.commons.lang3.RandomUtils;

public class SubstanciaNigraState implements State {
	
	// perception (internal state)
	private int degeneratedNeuronCount;
	private int stressedNeuronCount;
	private int inflammatedMicrogliaCount;
	private int actualDegenNeuron; // degen count
	private double averageNeuronHealth;
	private double currentGLP1dose;
	
	public SubstanciaNigraState(int deg, int str, int inf, int neu, double hea, double dos) {
		this.degeneratedNeuronCount = deg;
		this.inflammatedMicrogliaCount = inf;
		this.stressedNeuronCount = str;
		this.averageNeuronHealth = hea;
		this.currentGLP1dose = dos;
		this.actualDegenNeuron = neu;
	}
	
	public void setDegeratedNeuron(int x) {
		degeneratedNeuronCount = x;
	}
	
	public double getCurrentGLP1dose() {
		return currentGLP1dose;
	}
	
	public int getActualDegenNeuron() {
		return actualDegenNeuron;
	}
	
	public void setActualDegenNeuron(int healthyNeuronCount) {
		this.actualDegenNeuron = healthyNeuronCount;
	}
	
	public void setCurrentGLP1dose(double currentGLP1dose) {
		this.currentGLP1dose = currentGLP1dose;
	}
	
	public void setStressedNeuron(int x) {
		stressedNeuronCount = x;
	}
	
	public double getAverageNeuronHealth() {
		return averageNeuronHealth;
	}
	
	public void setAverageNeuronHealth(double averageNeuronHealth) {
		this.averageNeuronHealth = averageNeuronHealth;
	}
	
	public void setInflammatedMicroglia(int x) {
		inflammatedMicrogliaCount = x;
	}
	
	public int getDegeneratedNeuron() {
		return degeneratedNeuronCount;
	}
	
	public int getStressedNeuron() {
		return stressedNeuronCount;
	}
	
	public int getInflammatedMicroglia() {
		return inflammatedMicrogliaCount;
	}
	
	@Override
	public int hashCode(){
      int result = degeneratedNeuronCount;
      result = result + 31 * actualDegenNeuron;
      result = result + 31 * stressedNeuronCount;
      //result = 31 * Double.valueOf(averageNeuronHealth).hashCode();
      //result = result + 31 * Double.valueOf(currentGLP1dose).hashCode();
      return result;
	}
	
	@Override
	public String toString(){
         return("Death Neuron: " + this.getDegeneratedNeuron()
         + ", Stressed Neuron: " + this.getStressedNeuron()
         + ", Actual Degen Neuron: " + this.getActualDegenNeuron()
         + ", AvgNeuronHealth: " + this.getAverageNeuronHealth()
         //+ ", Inflammed Microglia: " + this.getInflammatedMicroglia()
         );
	}
	
   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      SubstanciaNigraState player = (SubstanciaNigraState) obj;
      return this.hashCode() == player.hashCode();
   }
	
	
}