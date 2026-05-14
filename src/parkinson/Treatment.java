package parkinson;
import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;




public class Treatment {
	
	private Context<Object> context;
	private Environment env;

	
	protected double resistence;
	protected Policy policy;
	private double dosage = 0;
	private double TotTreatmentDuration = 0;
	private double efficacy;
	private double dosageEvaporation;
	private double glialRedutionTreshold; // Tasso di riduzione del treshold 
	private double neuronDegenerationRateMod;
	
	public Treatment(Context<Object> context, Environment env, Policy policy) {
		this.context = context;
		this.policy = policy;
		this.env = env;
		// TODO this.efficacy = ;
	}
	
	
	public void somministrateGLP1(double dosage) {
		this.dosage = this.dosage + dosage;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void step()
	{
		// Cytokine rate update
		double rateModifier = this.dosage * this.policy.getDiffusionRateMod();
		this.env.setDiffusionRate(env.getCytokineDiffuser(), rateModifier);
		this.dosage = this.dosage * this.dosageEvaporation;
		
		// GlialCell treshold update
		double oldTreshold = this.policy.getCytoActivationTreshold();
		double newTreshold = oldTreshold * this.glialRedutionTreshold;
		policy.setCytoActivationTreshold(newTreshold);
		
		// HealthReductionRate
		double oldValue = this.policy.getDegenerationRate();
		double newValue = oldValue * this.neuronDegenerationRateMod;
		this.policy.setDegenerationRate(newValue);
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
