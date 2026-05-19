package parkinson;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import parkinson.Policy.StatType;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;




public class Treatment extends PassiveAgent {

	
	protected double resistence;
	protected Policy policy;
	
	private double GLP1dosage = 0;
	private double NLRP3dosage = 0;
	private double efficacy;
	private final double GLP1dosageEvaporation = 0.8f;
	private double NLRP3dosageEvaporation;
	private double glialRedutionTreshold; // Tasso di riduzione del treshold 
	private double neuronDegenerationRateMod;
	
	private double GLP1somministrationNumber = 0;
	private double NLRP3somministrationNumber = 0;
	
	// Learning Model
	private SubstanciaNigraState currentState;
	private List<Action> possibleActions;
	private final int NUM_ACTIONS = 21;     
	
	
	
	public Treatment(Context<Object> context) {
		super(context);
		this.policy = Policy.getInstance();
		// TODO this.efficacy = ;
	
		
		this.possibleActions = initializeDiscreteActions();	
		this.currentState = new SubstanciaNigraState(0, 0, 0);
	}
	
	
	public void somministrateGLP1(double dosage) {
		this.GLP1dosage = this.GLP1dosage + dosage;
		// Per le statistiche
		this.GLP1somministrationNumber++;
	}
	
	public void somministrateNLRB3(double dosage) {
		this.NLRP3dosage = this.NLRP3dosage + dosage;
		// Per le statistiche
		this.NLRP3somministrationNumber++;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 5)
	public void stepPerception() {
		IndexedIterable<Object> currentNeurons = context.getObjects(Neuron.class);
	
		for(Object s : currentNeurons) {
			Neuron d = (Neuron) s;
			
			if(d.getState() == NeuronState.DEGENERATED_DEATH) this.currentState.setDegeratedNeuron(this.currentState.getDegeneratedNeuron()+1);
			if(d.getState() == NeuronState.STRESSED) this.currentState.setStressedNeuron(this.currentState.getStressedNeuron()+1);
		}
		
		IndexedIterable<Object> currentMicroglias = context.getObjects(Microglia.class);
		
		for(Object s : currentMicroglias) {
			Microglia d = (Microglia) s;
			
			if(d.isInflammated()) this.currentState.setInflammatedMicroglia(this.currentState.getInflammatedMicroglia()+1);
		}
	}
	
	private double dosageAction() {
		double deathWeight = 0.9;
		double stressedWeight = 0.2;
		double base = 0.1;
		double dosage = (deathWeight * this.currentState.getDegeneratedNeuron() + stressedWeight * this.currentState.getStressedNeuron()) * base;
		
		return dosage;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void stepAction() {
		if(this.currentState.getDegeneratedNeuron() > 0) {
			somministrateGLP1(dosageAction());
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void step()
	{
		// GLP1
		double cytokineRateModifier = (1 + Math.log(this.GLP1dosage));
		double degeneratedNeuronRateModifier = (1 + Math.log(this.GLP1dosage));
		System.out.println(this.GLP1dosage);
		System.out.println("Health deg rate"+degeneratedNeuronRateModifier);
		// Cytokine rate update
		this.policy.getParam(StatType.CYTO_RELEASE_RATE).setModifier(cytokineRateModifier);
		this.policy.getParam(StatType.DEGENERATION_RATE).setModifier(degeneratedNeuronRateModifier);
		
		
		// Evaporazione/assorbimento farmaco (riduzione dose)
		if(this.GLP1dosage <= this.GLP1dosageEvaporation)
			this.GLP1dosage = 0;
		else
			this.GLP1dosage = this.GLP1dosage * GLP1dosageEvaporation;
		
		
		// NLRP3
		if(this.NLRP3dosage >= 0) {
			this.policy.setNLRB3inibitor(true);
			if(this.NLRP3dosage <= this.NLRP3dosageEvaporation)
				this.NLRP3dosage = 0;
			else
				this.NLRP3dosage = this.NLRP3dosage - this.NLRP3dosageEvaporation;
		}else {
			this.policy.setNLRB3inibitor(false);
		}
	}
	
	private List<Action> initializeDiscreteActions() {
		List<Action> l = new LinkedList<>();
		for (int i = 0; i < NUM_ACTIONS; i++) {
            l.add(new Dosage((double) i / (NUM_ACTIONS - 1)));   // 0.0 → 1.0 inclusi
        }
        return l;
	}
	

	private class SubstanciaNigraState implements State {
		
		// perception (internal state)
		private int degeneratedNeuronCount;
		private int stressedNeuronCount;
		private int inflammetedMicrogliaCount;
		
		public SubstanciaNigraState(int deg, int str, int inf) {
			this.degeneratedNeuronCount = deg;
			this.inflammetedMicrogliaCount = inf;
			this.stressedNeuronCount = str;
		}
		
		public void setDegeratedNeuron(int x) {
			degeneratedNeuronCount = x;
		}
		
		
		public void setStressedNeuron(int x) {
			stressedNeuronCount = x;
		}
		
		
		public void setInflammatedMicroglia(int x) {
			inflammetedMicrogliaCount = x;
		}
		
		public int getDegeneratedNeuron() {
			return degeneratedNeuronCount;
		}
		
		public int getStressedNeuron() {
			return stressedNeuronCount;
		}
		
		public int getInflammatedMicroglia() {
			return inflammetedMicrogliaCount;
		}
		
		@Override
		public int hashCode(){
	      int result = degeneratedNeuronCount;
	      result = 31 * inflammetedMicrogliaCount;
	      result = 31 * stressedNeuronCount;
	      return result;
		}
		
	}
	
	private class Dosage implements Action {
		
		private double dosage;
		
		public Dosage(double x) {
			dosage = x;
		}

		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return String.valueOf(dosage);
		}
		
		@Override
		public int hashCode(){
	      int result;
	      result = 31 * (int) dosage;
	      return result;
		}
		
	}
	
	

	/* 
	 TODO Parametri
	 context:
	 - numero di neuroni in stato stressato/morte
	 - numero di glial cells in stato infiammatorio
	
	Parametri del treatment:
	-dosaggio
	-numero di somministrazioni effettuate???
	
	Parametri dei farmaci:
	GLP-1:
	- tasso di riduzione della produzione di citochine
	- tasso di riduzione dei treshold delle cellule gliali
	- tasso di riduzione della perdita di health dei neuroni
	
	Inibitori NLRB3:
	- Sospensione dello stato infiammatorio delle cellule gliali con il conseguente stop della produzione di citochine
	
	 */
	
	// TODO Perception
	/*
	 - numero di neuroni in stato stressato/morte
	 - numero di glial cells in stato infiammatorio
	*/
	
	// TODO Azioni
	/*
	 * - somministra GLP-1
		 * 	- modifica il treshold di rilevazione citochine nelle cellule gliali
		 *  - modifica il rate di produzione citochine nelle cellule gliali
		 *  - modifica il rate di health reduction nei neuroni
	 * - somministra inibitori NLRB3
	 * 		- forza il blocco dello stato infiammatorio nelle cellule gliali
	 */
	
}
