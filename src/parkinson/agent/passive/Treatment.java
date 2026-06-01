package parkinson.agent.passive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import parkinson.Policy;

import parkinson.Policy.StatType;
import parkinson.agent.active.Microglia;
import parkinson.agent.active.Neuron;
import parkinson.learning.*;
import parkinson.utils.*;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.util.collections.IndexedIterable;

/*
 * TODO
 * 
 * 1. RISOLVERE CHE L EPISODIO TERMINA QUANDO SI INCASTRANO IN UN PUNTO
 * 2. Il fatto che il modello impari che in certi stati sia meglio un dosaggio "apparentemente" sbagliato è semplicemente dovuto alla stocasticità
 * delle posizioni delle cellule -> è possibile sintetizzarla nello stato?
 * 
 */

public class Treatment extends PassiveAgent {
  	public static final String PATH = "D:\\Informatica\\UniCAM\\Anno IV\\Multi Agent Systems and DDC\\ParkinsonAgentModel\\outputKami";
	public static final String JSON_NAME = "learnMap.json";
	public static final String CSV_NAME = "rlConvergence.csv";
	
	protected double resistence;
	protected Policy policy;
	private PolicyMode policyMode = PolicyMode.RL;  // Default: RL learning
	
	private double GLP1dosage = 0;
	private double toxicity = 0;
	private double NLRP3dosage = 0;
	private double efficacy;
	private final double GLP1dosageEvaporation = 0.9f;
	private double NLRP3dosageEvaporation;
	private double glialRedutionTreshold; // Tasso di riduzione del treshold 
	private double neuronDegenerationRateMod;
	private boolean modelActive = false;
	private double epsilonProb = 0.8;
	
	private double cumulativeDosage = 0f;
	private double cumulativeAvgNeuronHP = 0f;
	
	private double GLP1somministrationNumber = 0;
	private double NLRP3somministrationNumber = 0;
	
	// Learning Model
	private SubstanciaNigraState currentState;
	private List<Action> possibleActions;
	private final int NUM_ACTIONS = 10;
	private static final double MAX_DOSE = 2.0d;
	private Dosage lastAction = null;
	private LearningModel rlModel;   
	
	private IndexedIterable<Object> currentNeurons;
	private int totNeurons;
	private int degenCount = 0;
	
	private int stationaryStep = 0;
	
	private double cumulativeReward = 0.0;
	private int healthyCount = 0;
	
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
	
	public double getGLP1dosage() {
		return GLP1dosage;
	}
	
	public double getToxicity() {
		return toxicity;
	}
		
	public void somministrateGLP1(double dosage) {
		//this.GLP1dosage = this.GLP1dosage + dosage;
      // Smoothing forte 
		System.out.println("Dosaggio " + this.GLP1dosage);
		System.out.println("Dosaggio " + this.currentState.getCurrentGLP1dose());
		this.GLP1dosage = this.GLP1dosage * 0.65 + 
				this.currentState.getCurrentGLP1dose() * (1 - 0.65);
		// Per le statistiche
		this.GLP1somministrationNumber++;
	}
	
	//@ScheduledMethod(start = 1, interval = 1, priority = 5)
	public void stepPerception() {
		// TODO update it with new state stuff except for the dose
		this.currentState.setDegeratedNeuron(0);
		this.currentState.setStressedNeuron(0);
		this.currentState.setInflammatedMicroglia(0);
		healthyCount = 0;
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
			if(d.getState() == NeuronState.STRESSED) {
				avgNeuronHealth += d.getHealth();
				this.currentState.setStressedNeuron(this.currentState.getStressedNeuron()+1);
			}
		}
		
		var x = totNeurons - healthyCount - this.currentState.getStressedNeuron();
		this.currentState.setDegeratedNeuron(x);


		this.currentState.setAverageNeuronHealth(avgNeuronHealth / totNeurons);
		this.currentState.setActualDegenNeuron(degenCount);
		IndexedIterable<Object> currentMicroglias = context.getObjects(Microglia.class);
		
		for(Object s : currentMicroglias) {
			Microglia d = (Microglia) s;
			
			if(d.isInflammated()) 
				this.currentState.setInflammatedMicroglia(this.currentState.getInflammatedMicroglia()+1);
		}
		
		System.out.println("NEURONI MORTI: " + degenCount);
		System.out.println("NEURONI STRESSATI: " + this.currentState.getStressedNeuron());
		System.out.println("MICROGLIE INFIAMMATE: " + this.currentState.getInflammatedMicroglia());
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void stepQAction() {
		// initial state fire up  
		if(RepastEssentials.GetTickCount() > 0
		//this.currentState.getDegeneratedNeuron() >= 1 
		//&& this.currentState.getStressedNeuron() >= 1 
		//&& this.currentState.getInflammatedMicroglia() == 0) 
	)	{
			modelActive = true;
		}
		
		if(modelActive
		) {
			lastAction = (Dosage) decideAction();
			this.currentState.setCurrentGLP1dose(lastAction.getDosage());
			somministrateGLP1(lastAction.getDosage());
		}
	}
	
	public Action decideAction() {
		switch (this.policyMode) {
			case NO_DOSE:
				return new Dosage(0.0);
				
			case MAX_DOSE:
				return new Dosage(MAX_DOSE);
				
			case PROPORTIONAL:
				return decideActionProportional();
				
			case RL:
			default:
				return decideActionRL();
		}
	}
	
	/**
	 * Policy: dose proporzionale a gravita' attuale dello stato
	 * Per stratificazione: a parita' di stato iniziale, questa dose e' deterministica
	 */
	private Action decideActionProportional() {
		int degeneratedCount = this.currentState.getDegeneratedNeuron();
		int stressedCount = this.currentState.getStressedNeuron();
		double deathWeight = 0.9;
		double stressedWeight = 0.2;

		// Dose proporzionale al numero di neuroni affetti (dead + stressed)
		double gravityRatio = (deathWeight * degeneratedCount + stressedWeight * stressedCount) / (double) totNeurons;
		double dose = Math.min(MAX_DOSE, gravityRatio * MAX_DOSE);
		
		return new Dosage(dose);
	}
	
	/**
	 * Q-Learning policy: esplorazione epsilon-greedy con decadimento
	 */
	private Action decideActionRL() {
		var rand = new Random();
		Action bestAction = null;
		double bestValue = Double.NEGATIVE_INFINITY; 
		epsilonProb = 0.8;
		
		double currentStep = RepastEssentials.GetTickCount();
		double stepDecay = Math.pow(0.999, currentStep);
		double runDecay = Math.pow(0.996, this.getBatchRunNumber());
		epsilonProb = Math.max(0.05, epsilonProb * runDecay);
		/*
		if (rand.nextDouble() < this.epsilonProb) {
	        return possibleActions.get(rand.nextInt(possibleActions.size()));
	    }*/
		
		// Per ogni azione possibile
		for (Action myAction : possibleActions) {
			System.out.println("- " + this.currentState.toString() + " " + myAction.toString());
			double actionValue = rlModel.getValue(new StateAction(this.currentState, myAction));
			System.out.println(actionValue);
			if (actionValue > bestValue) {
				bestValue = actionValue;
				bestAction = myAction;
			}
		}

	   return bestAction == null ? possibleActions.get(rand.nextInt(possibleActions.size())) : bestAction;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 5)
	public void updateModel() {
		SubstanciaNigraState oldState = new SubstanciaNigraState(currentState.getDegeneratedNeuron(), currentState.getStressedNeuron(), 
				currentState.getInflammatedMicroglia(), currentState.getActualDegenNeuron(),
				currentState.getAverageNeuronHealth(), currentState.getCurrentGLP1dose());

		// observe new state
		stepPerception();

		if(lastAction != null) {
			double reward = this.calculateReward(oldState);
			cumulativeReward += reward;
			cumulativeDosage += this.getGLP1dosage();
			cumulativeAvgNeuronHP += this.currentState.getAverageNeuronHealth();

			rlModel.updateValue(new StateAction(oldState, lastAction), currentState, reward);

		}
		boolean isTerminal = this.isTerminalState();

		if(isTerminal) {
			this.dataWrite();
			RunEnvironment.getInstance().endRun();
		}
	}

	private boolean isTerminalState() {
		int currentStep = (int) RepastEssentials.GetTickCount();
		
		// Stato vincente: infiammazione contenuta dopo warmup
		if(degenCount > 0 || this.currentState.getStressedNeuron() > 0)
				stationaryStep = 0;
		else
			stationaryStep++;
		
		boolean successTerminal = (stationaryStep == 50);
		
		// Stato di timeout
		boolean timeoutTerminal = (currentStep == 1200);
		
		return successTerminal || timeoutTerminal;
	}
	
  	public void dataWrite() {
		FileManager.save(PATH + JSON_NAME, this.rlModel.actionValues);
		System.out.println("Map has been saved.");

		FileManager.logEpisodeData(
			PATH + CSV_NAME,
			RunEnvironment.getInstance().getParameters().getInteger("randomSeed") + "," + 
    	    		this.cumulativeReward + "," + 
    	    		this.currentState.getDegeneratedNeuron() + "," + 
    	    		(this.cumulativeAvgNeuronHP / RepastEssentials.GetTickCount()) + "," + 
    	    		(this.cumulativeDosage / RepastEssentials.GetTickCount()) + "," + 
    	    		this.epsilonProb + "," +
    	    		RepastEssentials.GetTickCount() + "\n"
		);
	}
	
	public double calculateReward(SubstanciaNigraState oldState) {
		final double DEATH_WEIGHT = -0.3;
		final double DEATH_WEIGHT_TOTAL = -0.4;
		final double STRESSED_WEIGHT = -0.2;  
		final double HEALTHY_WEIGHT = 1.3;
		final double DOSAGE_WEIGHT = -0.3;
		final double TREND_WEIGHT = -2.0;     // Ridurre aggressività
		final double IMPROVEMENT_BONUS = 1.0; // Normalizzare
		final double DOSE_BONUS = 0.8; // Normalizzare
		final int TOTAL_NEURONS = totNeurons; // Usare variabile anziché hardcoded 31
		
		double doseCost = DOSAGE_WEIGHT * (this.currentState.getCurrentGLP1dose() / MAX_DOSE);
		double deathPenalty = DEATH_WEIGHT * (this.currentState.getActualDegenNeuron() / (double) TOTAL_NEURONS);
		double deathPenalty2 = DEATH_WEIGHT_TOTAL * (this.currentState.getDegeneratedNeuron() / (double) TOTAL_NEURONS);
		
		int deltaD = this.currentState.getDegeneratedNeuron() - oldState.getDegeneratedNeuron();
		double trendPenalty = TREND_WEIGHT * Math.max(0, deltaD / (double) TOTAL_NEURONS);				
		double doseBonus = 0.0;
		int totalInflammation = currentState.getDegeneratedNeuron() + 
								currentState.getStressedNeuron() + 
								currentState.getInflammatedMicroglia();
		
		// Se usiamo il farmaco E l'infiammazione non peggiora → bonus proporzionale alla gravità
		if (this.currentState.getCurrentGLP1dose() > 0.0) {
			if (deltaD <= 0) {
				// Il farmaco sta aiutando: bonus forte basato su quanto grave era la situazione
				doseBonus = DOSE_BONUS * (totalInflammation / (double) TOTAL_NEURONS) * 
						   (this.currentState.getCurrentGLP1dose() / MAX_DOSE);
			} else {
				// Il farmaco non sta aiutando: piccolo malus per uso inefficace
				doseBonus = -0.1;
			}
		}
		
		double improvementBonus = 0.0;
		if (deltaD == 0 && this.currentState.getCurrentGLP1dose() > 0.1 * MAX_DOSE) {
			improvementBonus += IMPROVEMENT_BONUS * 0.5;
		}
		
		double totalReward = doseCost + deathPenalty + trendPenalty + improvementBonus + deathPenalty2 + doseBonus;
		
		// DEBUG: Log della composizione del reward
		if (currentState.getCurrentGLP1dose() > 0 || degenCount > 0) {
			/*System.out.println("[REWARD BREAKDOWN] Run " + getBatchRunNumber() 
				+ " | Dose: " + String.format("%.2f", currentState.getCurrentGLP1dose())
				+ " | doseCost: " + String.format("%.3f", doseCost)
				+ " | deathPenalty: " + String.format("%.3f", deathPenalty)
				+ " | trendPenalty: " + String.format("%.3f", trendPenalty)
				+ " | doseBonus: " + String.format("%.3f", doseBonus)
				+ " | TOTAL: " + String.format("%.3f", totalReward));*/
		}
		
		return totalReward;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void step()
	{

		// GLP1
		double rateModifier;
		if (this.GLP1dosage < 1.6) {
		    // Prima: logaritmica (conservativa)
		    rateModifier = 1 + Math.log(1 + this.GLP1dosage);
		} else {
		    // Dopo 1.6: crescita più ripida
		    double baseValue = 1 + Math.log(1 + 1.6);  // ~1.86
		    double excessDose = this.GLP1dosage - 1.6;
		    rateModifier = baseValue + 2 * excessDose;  // +2x per ogni unità sopra 1.6
		}
		
		// Cytokine rate update
		this.policy.getParam(StatType.CYTO_RELEASE_RATE).setModifier(1 / rateModifier);
		// DegenerateNeuronRate
		this.policy.getParam(StatType.DEGENERATION_RATE).setModifier(1 / rateModifier);
		
		// 1 / (1 + e ^ -dosaggio)
		
		this.policy.getParam(StatType.CYTO_NEURON_THRESHOLD).setModifier(rateModifier);
		this.policy.getParam(StatType.CYTO_MICROGLIA_THRESHOLD).setModifier(rateModifier);
		
		this.policy.getParam(StatType.EVAPORATION_RATE).setModifier(1.0d - (rateModifier / 30));

		this.env.setEvaporationRate(this.env.getCytokineDiffuser(), this.policy.getParam(StatType.EVAPORATION_RATE).getEffectiveValue());
		System.out.println("Valore CYTO_RELEASE_RATE " + this.policy.getParam(StatType.CYTO_RELEASE_RATE).getEffectiveValue());	
		System.out.println("Valore DEGENERATION_RATE " + this.policy.getParam(StatType.DEGENERATION_RATE).getEffectiveValue());	
		System.out.println("Valore CYTO_NEURON_THRESHOLD " + this.policy.getParam(StatType.CYTO_NEURON_THRESHOLD).getEffectiveValue());	
		System.out.println("Valore CYTO_MICROGLIA_THRESHOLD " + this.policy.getParam(StatType.CYTO_MICROGLIA_THRESHOLD).getEffectiveValue());
		System.out.println("Valore EVAPORATION_RATE " + this.policy.getParam(StatType.EVAPORATION_RATE).getEffectiveValue());

		//System.out.println("Dosaggio " + this.GLP1dosage);
		toxicity = (Math.exp((this.GLP1dosage - 1.0f/4.0f) - 1)) / 128;
		toxicity = Math.min(toxicity, 4.0f);
		System.out.println("toxic a: " + toxicity);
		/*
		// Evaporazione/assorbimento farmaco (riduzione dose)
		if(this.GLP1dosage <= this.GLP1dosageEvaporation)
			this.GLP1dosage = 0;
		else
			this.GLP1dosage = this.GLP1dosage - this.currentState.getCurrentGLP1dose() * GLP1dosageEvaporation; // evap of dosage equal to 95% of last dosage
		*/
	}
	
	private List<Action> initializeDiscreteActions() {
		List<Action> l = new LinkedList<>();
		for (int i = 0; i <= NUM_ACTIONS; i++) {
            l.add(new Dosage(i * (MAX_DOSE / 10)));   // 0.0 → 1.0 inclusi
        }
        return l;
	}
	
	public static int getBatchRunNumber() {
	    if(RunState.getInstance().getRunInfo().isBatch()) {
	    	return RunState.getInstance().getRunInfo().getRunNumber();
	    }
	    return 1; 
	}

	/**
	 * Enum per stratificazione causale: diverse policy di dosaggio
	 */
	public enum PolicyMode {
		RL("RL"), 
		NO_DOSE("NoDose"), 
		MAX_DOSE("MaxDose"), 
		PROPORTIONAL("Proportional");
		
		private String name;
		PolicyMode(String name) { this.name = name; }
		public String getName() { return name; }
	}
}