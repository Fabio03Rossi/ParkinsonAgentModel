package parkinson;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import parkinson.Policy.StatType;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Treatment extends PassiveAgent {
	protected double resistence;
	protected Policy policy;
	
	private double GLP1dosage = 0;
	private double NLRP3dosage = 0;
	private double efficacy;
	private final double GLP1dosageEvaporation = 1f;
	private double NLRP3dosageEvaporation;
	private double glialRedutionTreshold; // Tasso di riduzione del treshold 
	private double neuronDegenerationRateMod;
	
	private double GLP1somministrationNumber = 0;
	private double NLRP3somministrationNumber = 0;
	
	// Learning Model
	private SubstanciaNigraState currentState;
	private List<Action> possibleActions;
	private final int NUM_ACTIONS = MAX_DOSE;
	private static final int MAX_DOSE = 20;
	private Dosage lastAction = null;
	private LearningModel rlModel;   
	
	private IndexedIterable<Object> currentNeurons;
	private int totNeurons;
	private int degenCount = 0;
	
	
	
	public Treatment(Context<Object> context) {
		super(context);
		this.policy = Policy.getInstance();
		
		// Initializing rl model	
		this.possibleActions = initializeDiscreteActions();	
		this.currentState = new SubstanciaNigraState(0, 0, 0, 0, 0, 0);
		this.rlModel = new TreatmentModel(this.possibleActions);
		
		currentNeurons = context.getObjects(Neuron.class);
		this.totNeurons = currentNeurons.size();
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
	
	//@ScheduledMethod(start = 1, interval = 1, priority = 5)
	public void stepPerception() {
		// TODO update it with new state stuff except for the dose
		this.currentState.setDegeratedNeuron(0);
		this.currentState.setStressedNeuron(0);
		this.currentState.setInflammatedMicroglia(0);
		int healthyCount = 0;
		degenCount = 0;
		double avgNeuronHealth = 0;
		for(Object s : currentNeurons) {
			Neuron d = (Neuron) s;
			
			if(d.getState() == NeuronState.HEALTHY) {
				healthyCount++;
				avgNeuronHealth += d.getHealth();
			}
			
			if(d.getState() == NeuronState.DEGENERATED_DEATH) {
				degenCount++;
			}
			if(d.getState() == NeuronState.STRESSED) this.currentState.setStressedNeuron(this.currentState.getStressedNeuron()+1);
		}
		
		var x = totNeurons - healthyCount - this.currentState.getStressedNeuron();
		this.currentState.setDegeratedNeuron(x);


		this.currentState.setAverageNeuronHealth(avgNeuronHealth / healthyCount);
		this.currentState.setHealthyNeuronCount(healthyCount);
		IndexedIterable<Object> currentMicroglias = context.getObjects(Microglia.class);
		
		for(Object s : currentMicroglias) {
			Microglia d = (Microglia) s;
			
			if(d.isInflammated()) 
				this.currentState.setInflammatedMicroglia(this.currentState.getInflammatedMicroglia()+1);
		}
		
		System.out.println("NEURONI MORTI: " + degenCount);
		System.out.println("NEURONI STRESSATI: " + this.currentState.getStressedNeuron());
		System.out.println("MICROGLIE MORTI: " + this.currentState.getInflammatedMicroglia());
	}
	
	private double dosageAction() {
		double deathWeight = 0.9;
		double stressedWeight = 0.2;
		double base = 0.5;
		double dosage = (deathWeight * this.currentState.getDegeneratedNeuron() + stressedWeight * this.currentState.getStressedNeuron()) * base;
		
		return dosage;
	}
	
	//@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void stepAction() {
		if(this.currentState.getDegeneratedNeuron() > 0) {
			somministrateGLP1(dosageAction());
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void stepQAction() {
		if(this.currentState.getDegeneratedNeuron() > 0) {
			lastAction = (Dosage) decideAction();
			somministrateGLP1(lastAction.getDosage());
			this.currentState.setCurrentGLP1dose(lastAction.getDosage());
		}
		if(RepastEssentials.GetTickCount() == 1200 || 
				(degenCount == 0 && this.currentState.getStressedNeuron() == 0 
				&& this.currentState.getInflammatedMicroglia() == 0 && RepastEssentials.GetTickCount() > 20))
		{
			this.save();
			RunEnvironment.getInstance().endRun();
		}
	}
	
	public Action decideAction() {
	      Action bestAction = null;
	      double bestValue = Double.MIN_VALUE; 
	      int zeroCount = 0;
	      double epsilonProb = 0.2;
	      
	      // Per ogni azione possibile
	      for (Action myAction : possibleActions) {
	         double actionValue = rlModel.getValue(new StateAction(this.currentState, myAction));
	         
	         // counting how many paths are currently not explored
	         if(actionValue == 0.0) {
	        	 zeroCount++;
	         }
	         
	         if (actionValue > bestValue) {
	               bestValue = actionValue;
	               bestAction = myAction;
	         }
	      }
	      

	      epsilonProb = epsilonProb + (zeroCount * 0.02);
	      
	    
	      boolean epsilonExp = new Random().nextInt(1, 11) <= epsilonProb * 10;
	      

	      if(epsilonExp) bestAction = possibleActions.get(new Random().nextInt(possibleActions.size()));
	      
	      return bestAction != null ? bestAction : possibleActions.get(new Random().nextInt(possibleActions.size()));
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 5)
	public void updateModel() {
		SubstanciaNigraState oldState = new SubstanciaNigraState(currentState.getDegeneratedNeuron(), currentState.getStressedNeuron(), 
				currentState.getInflammatedMicroglia(), currentState.getHealthyNeuronCount(),
				currentState.getAverageNeuronHealth(), currentState.getCurrentGLP1dose());
		stepPerception();
		if(lastAction != null) {
			double reward = this.calculateReward(oldState);
			rlModel.updateValue(new StateAction(oldState, lastAction), currentState, reward);
		}
	}
	
	public double reward(SubstanciaNigraState oldState) {
		double deathWeight = 0.9;
		double stressedWeight = 0.2;
		double healthyWeight = 0.3;
		double dosageWeight = 0.5;
		double avgHealthDiff = currentState.getAverageNeuronHealth() - oldState.getAverageNeuronHealth();
		double reward = - (deathWeight * this.currentState.getDegeneratedNeuron() + stressedWeight * this.currentState.getStressedNeuron())
				- (dosageWeight * this.currentState.getCurrentGLP1dose()) + (healthyWeight * this.currentState.getHealthyNeuronCount());
		
		return reward;
		
	}
	
	public double calculateReward(SubstanciaNigraState oldState) {
		double deathWeight = -2.9;
		double stressedWeight = 0.2;
		double healthyWeight = 2.3;
		double dosageWeight = -0.5;
		double reward = 0.0;
		
		// 1. Penalità per dose (ma non troppo aggressiva)
		double doseCost = dosageWeight * (this.currentState.getCurrentGLP1dose() / MAX_DOSE);                    // tra -0.8 e 0
		
		// 2. Penalità per cellule morte assolute
		double deathPenalty = deathWeight * (this.currentState.getDegeneratedNeuron() / (double) 31);
		
		// 3. Ricompensa per cellule vive (o penalità per perdita di popolazione)
		double liveReward = healthyWeight * (this.currentState.getHealthyNeuronCount() / (double) 31);
		
		// 4. SEGNALE FORTEMENTE IMPORTANTE: Trend della morte (differenza temporale)
		int deltaD = this.currentState.getDegeneratedNeuron() - oldState.getDegeneratedNeuron();
		double trendPenalty = -3.5 * Math.max(0, deltaD / (double) 31);   // forte penalità se morte aumenta
		
		// 5. Bonus per riduzione della morte o dello stress
		int deltaS = this.currentState.getStressedNeuron() - oldState.getStressedNeuron();
		double improvementBonus = 0.0;
		if (deltaD < 0) improvementBonus += 2.8;           // morte diminuisce → buon segnale
		if (deltaS < -5) improvementBonus += 1.5;          // stress diminuisce
		
		// 6. Bonus di controllo (quando la dose sta funzionando)
		if (this.currentState.getCurrentGLP1dose() > 0.1 * MAX_DOSE && deltaD <= 0) {
		improvementBonus += 1.8;   // "hai fatto la cosa giusta"
		}
		
		// Composizione finale
		reward = doseCost 
		+ deathPenalty 
		+ liveReward 
		+ trendPenalty 
		+ improvementBonus;
		
		// Bonus terminale (da applicare solo alla fine dell'episodio)
		// if (episodio finito) reward += 5.0 * (L_current / (double) totalCells);
		
		return reward;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void step()
	{
		// GLP1
		double rateModifier = (1 + Math.log(1 + this.GLP1dosage));
		
		// Cytokine rate update
		this.policy.getParam(StatType.CYTO_RELEASE_RATE).setModifier(rateModifier);
		// DegenerateNeuronRate
		this.policy.getParam(StatType.DEGENERATION_RATE).setModifier(rateModifier);
		
		// 1 / (1 + e ^ -dosaggio)
		
		this.policy.getParam(StatType.CYTO_NEURON_THRESHOLD).setModifier(rateModifier);
		
		
		
		System.out.println("Dosaggio " + this.GLP1dosage);
		
		// Evaporazione/assorbimento farmaco (riduzione dose)
		if(this.GLP1dosage <= this.GLP1dosageEvaporation)
			this.GLP1dosage = 0;
		else
			this.GLP1dosage = this.GLP1dosage - this.currentState.getCurrentGLP1dose() * GLP1dosageEvaporation; // evap of dosage equal to 95% of last dosage
		
		/*
		// NLRP3
		if(this.NLRP3dosage >= 0) {
			this.policy.setNLRB3inibitor(true);
			if(this.NLRP3dosage <= this.NLRP3dosageEvaporation)
				this.NLRP3dosage = 0;
			else
				this.NLRP3dosage = this.NLRP3dosage - this.NLRP3dosageEvaporation;
		}else {
			this.policy.setNLRB3inibitor(false);
		}*/
	}
	
	private List<Action> initializeDiscreteActions() {
		List<Action> l = new LinkedList<>();
		for (int i = 0; i < NUM_ACTIONS; i++) {
            l.add(new Dosage(i * 1));   // 0.0 → 1.0 inclusi
        }
        return l;
	}
	

	public void save() {
		System.out.println("Map has been saved.");
		this.rlModel.save("learnMap.json");
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
