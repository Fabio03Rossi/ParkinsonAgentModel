 package parkinson;

 /**
  * La Policy determina le caratteristiche mutabili della simulazione.
  * Tenendo conto della fisiologia del paziente e degli stimuli esterni per calcolare
  * i corretti moltiplicatori.
  */
public class Policy {
	// Treatment
	protected boolean NLRB3inibitor;	 // Utilizzata per indicare se nel sistema è presente una quantità 
										 //sufficiente di inibitore per bloccare lo stato infiammatorio delle cellule gliali
	
	// Microglia
	protected double cytoActivationTreshold;
	protected double cytoReleaseRate;
	protected double cytoPerceptionRange;
	
	
	// Neuroni
	protected double alphaSinucleinTreshold;
	protected double cytokineTreshold;
	protected double degenerationRate;
	
	
	public Policy(int age, boolean gender, HealthDisease healthAlteration, double cytoActTre, double alphaTre, double cytoTre) {
		this.cytoActivationTreshold = cytoActTre;
		this.alphaSinucleinTreshold = alphaTre;
		this.cytokineTreshold = cytoTre;
	}
	
	
	public double getDegenerationRate() {
		return degenerationRate;
	}
	
	public void setDegenerationRate(double degenerationRate) {
		this.degenerationRate = degenerationRate;
	}
	
	public double getAlphaSinucleinTreshold() {
		return alphaSinucleinTreshold;
	}
	
	public void setAlphaSinucleinTreshold(double alphaSinucleinTreshold) {
		this.alphaSinucleinTreshold = alphaSinucleinTreshold;
	}

	public double getCytoActivationTreshold() {
		return cytoActivationTreshold;
	}
	
	public void setCytoActivationTreshold(double cytoActivationTreshold) {
		this.cytoActivationTreshold = cytoActivationTreshold;
	}
	
	public double getCytokineTreshold() {
		return cytokineTreshold;
	}
	
	public void setCytokineTreshold(double cytokineTreshold) {
		this.cytokineTreshold = cytokineTreshold;
	}
	
	public boolean isNLRB3inibitor() {
		return NLRB3inibitor;
	}
	
	public void setNLRB3inibitor(boolean nLRB3inibitor) {
		NLRB3inibitor = nLRB3inibitor;
	}
	
	// -----------------------------------------------------------------------------------
	
	public double getNeuronDeathRate() {
		return 1.0f;
	}
	
	public double getMicrogliaActivationThreshold() {
		return 1.0f;
	}
	
	public double getDiffusionRateMod() {
		return 1.0f;
	}
	
	public double getEvaporationRateMod() {
		return 1.0f;
	}
	
	public double getCytoPerceptionRange() {
		return 1.0f;
	}
	
	public double getCytoReleaseRate() {
		return 1.0f;
	}

}
