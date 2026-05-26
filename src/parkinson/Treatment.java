package parkinson;
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

import parkinson.Policy.StatType;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/*
 * TODO
 * 
 * 1. RISOLVERE CHE L EPISODIO TERMINA QUANDO SI INCASTRANO IN UN PUNTO
 * 2. Il fatto che il modello impari che in certi stati sia meglio un dosaggio "apparentemente" sbagliato è semplicemente dovuto alla stocasticità
 * delle posizioni delle cellule -> è possibile sintetizzarla nello stato?
 * 
 */

public class Treatment extends PassiveAgent {
	public static final String PATH = "C:\\Users\\theca\\Desktop\\unicam\\DCC&MAS\\ParkinsonAgentModel\\tabOutput";
	
	protected double resistence;
	protected Policy policy;
	
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
	
	private double GLP1somministrationNumber = 0;
	private double NLRP3somministrationNumber = 0;
	
	// Learning Model
	private SubstanciaNigraState currentState;
	private List<Action> possibleActions;
	private final int NUM_ACTIONS = MAX_DOSE;
	private static final int MAX_DOSE = 10;
	private Dosage lastAction = null;
	private LearningModel rlModel;   
	
	private IndexedIterable<Object> currentNeurons;
	private int totNeurons;
	private int degenCount = 0;
	
	private int stationaryStep = 0;
	
	private double cumulativeReward = 0.0;
	
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
	
	private double dosageAction() {
		double deathWeight = 0.9;
		double stressedWeight = 0.2;
		double base = 0.5;
		double dosage = (deathWeight * this.currentState.getDegeneratedNeuron() + stressedWeight * this.currentState.getStressedNeuron()) * base;
		dosage = Math.max(dosage, 1.0f);
		return dosage;
	}
	
	//@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void stepAction() {
		// initial state fire up  
		if(this.currentState.getDegeneratedNeuron() >= 2 
		&& this.currentState.getStressedNeuron() >= 1 
		//&& this.currentState.getInflammatedMicroglia() == 0) 
	)	{
			modelActive = true;
		}
		
		if(modelActive
		) {
			this.currentState.setCurrentGLP1dose(dosageAction());
			somministrateGLP1(dosageAction());
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void stepQAction() {
		// initial state fire up  
		if(this.currentState.getDegeneratedNeuron() >= 2 
		&& this.currentState.getStressedNeuron() >= 1 
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
		var rand = new Random();
		Action bestAction = null;
		double bestValue = Double.NEGATIVE_INFINITY; 
		int zeroCount = 0;
		epsilonProb = 0.8;
		
		double currentStep = RepastEssentials.GetTickCount();
		double stepDecay = Math.pow(0.999, currentStep);
		double runDecay = Math.pow(0.996, this.getBatchRunNumber());
		epsilonProb = Math.max(0.05, epsilonProb /** stepDecay*/ * runDecay);
		
		//epsilonProb += zeroCount * 0.02;	   
		
		if (rand.nextDouble() < this.epsilonProb) {
	        return possibleActions.get(rand.nextInt(possibleActions.size()));
	    }
		
		// Per ogni azione possibile
		for (Action myAction : possibleActions) {
			System.out.println("- " + this.currentState.toString() + " " + myAction.toString());
			double actionValue = rlModel.getValue(new StateAction(this.currentState, myAction));
			System.out.println(actionValue);
			// counting how many paths are currently not explored
			/*if(actionValue == 0.0) {
				zeroCount++;
			}*/
			
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

			rlModel.updateValue(new StateAction(oldState, lastAction), currentState, reward);

		}
		boolean isTerminal = this.isTerminalState();

		if(isTerminal) {
			this.save();
			this.logEpisodeData();
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
	
	private void logEpisodeData() {
		var path = "rl_convergence.csv";
		var f = new File(PATH + path);
		try {
			f.createNewFile();
    	    FileWriter f2 = new FileWriter(f, true);
    	    f2.write(RunEnvironment.getInstance().getParameters().getInteger("randomSeed") + "," + 
    	    		this.cumulativeReward + "," + 
    	    		this.currentState.getDegeneratedNeuron() + "," + 
    	    		(this.cumulativeDosage / RepastEssentials.GetTickCount()) + "," + 
    	    		this.epsilonProb + "," +
    	    		RepastEssentials.GetTickCount()
    	    		+ "\n");
    	    f2.close();
		} catch (IOException e) {
			e.printStackTrace();
    	}
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
		if (deltaD < 0) improvementBonus += IMPROVEMENT_BONUS * 0.7;
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
		double rateModifier = (1 + Math.log(1 + this.GLP1dosage));
		
		// Cytokine rate update
		this.policy.getParam(StatType.CYTO_RELEASE_RATE).setModifier(1 / rateModifier);
		// DegenerateNeuronRate
		this.policy.getParam(StatType.DEGENERATION_RATE).setModifier(1 / rateModifier);
		
		// 1 / (1 + e ^ -dosaggio)
		
		this.policy.getParam(StatType.CYTO_NEURON_THRESHOLD).setModifier(rateModifier);
		this.policy.getParam(StatType.CYTO_MICROGLIA_THRESHOLD).setModifier(rateModifier);
		
		this.policy.getParam(StatType.EVAPORATION_RATE).setModifier(1.0d - (rateModifier / 50));

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
		for (int i = 0; i <= NUM_ACTIONS; i++) {
            l.add(new Dosage(i * 0.2));   // 0.0 → 1.0 inclusi
        }
        return l;
	}
	
	public void save() {
		System.out.println("Map has been saved.");
		this.rlModel.save("learnMap.json");
	}
	
	public int getBatchRunNumber() {
	    if(RunState.getInstance().getRunInfo().isBatch()) {
	    	return RunState.getInstance().getRunInfo().getRunNumber();
	    }
	    return 1; 
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
