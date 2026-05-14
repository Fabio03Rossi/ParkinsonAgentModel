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
	
	private double GLP1dosage = 0;
	private double NLRP3dosage = 0;
	private double efficacy;
	private double GLP1dosageEvaporation;
	private double NLRP3dosageEvaporation;
	private double glialRedutionTreshold; // Tasso di riduzione del treshold 
	private double neuronDegenerationRateMod;
	
	private double GLP1somministrationNumber = 0;
	private double NLRP3somministrationNumber = 0;

	
	public Treatment(Context<Object> context, Environment env) {
		this.context = context;
		this.policy = env.getPolicy();
		this.env = env;
		// TODO this.efficacy = ;
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
	
	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	public void step()
	{
		// GLP1
		// Cytokine rate update
		double rateModifier = this.GLP1dosage * this.policy.getDiffusionRateMod();
		this.env.setDiffusionRate(env.getCytokineDiffuser(), rateModifier);
		this.GLP1dosage = this.GLP1dosage * this.GLP1dosageEvaporation;
		
		// GlialCell treshold update
		double oldTreshold = this.policy.getCytoActivationTreshold();
		double newTreshold = oldTreshold * this.glialRedutionTreshold;
		policy.setCytoActivationTreshold(newTreshold);
		
		// HealthReductionRate
		double oldValue = this.policy.getDegenerationRate();
		double newValue = oldValue * this.neuronDegenerationRateMod;
		this.policy.setDegenerationRate(newValue);
		
		// Evaporazione/assorbimento farmaco (riduzione dose)
		if(this.GLP1dosage <= this.GLP1dosageEvaporation)
			this.GLP1dosage = 0;
		else
			this.GLP1dosage = this.GLP1dosage - this.GLP1dosageEvaporation;
		
		
		
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
