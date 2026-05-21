package parkinson;

import parkinson.Policy.StatType;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class GlialCell extends Agent{

	protected GlialState state;
	protected boolean infiammatoryState; //true per stato infiammatorio, false per stato non infiammato
	
	protected double cytokinesReceived;
	protected double activationThreshold;
	protected double cytokineReleaseRate;
	
	protected Policy policy;
	// LAYERS
	protected GridValueLayer cytokineLayer;
	
	public GlialCell(Context context) {
		super(context);
		
		this.policy = Policy.getInstance();
		this.state = GlialState.RESTING;
		
		this.cytokineReleaseRate = this.policy.getParam(Policy.StatType.CYTO_RELEASE_RATE).getEffectiveValue();
		this.activationThreshold = this.policy.getParam(Policy.StatType.CYTO_MICROGLIA_THRESHOLD).getEffectiveValue();
		this.infiammatoryState = this.policy.isNLRB3inibitor();
		
		this.cytokineLayer = (GridValueLayer) context.getValueLayer("cytoLayer");


	}
	
	
	public void cytokineRelease() {
		// Ottengo la posizione dalla griglia
		   int x = this.grid.getLocation(this).getX();
		   int y = this.grid.getLocation(this).getY();
	    	   double cytokineValue = cytokineLayer.get(x,y);
	    	   // Setto il nuovo valore tenendo in considerazione il cytoReleaseRate da policy
	    	   System.out.println("Valore cytokineValue " + cytokineValue);
	    	   System.out.println("Valore cytomodifier " + this.policy.getParam(StatType.CYTO_RELEASE_RATE).getEffectiveValue());
	    	   System.out.println("Valore cytorelease " + cytokineValue + 1 / this.policy.getParam(StatType.CYTO_RELEASE_RATE).getEffectiveValue());	    	   
	    	   cytokineLayer.set(cytokineValue + this.policy.getParam(StatType.CYTO_RELEASE_RATE).getEffectiveValue(), x, y);
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	public void updateValues() {
		this.cytokineReleaseRate = this.policy.getParam(Policy.StatType.CYTO_RELEASE_RATE).getEffectiveValue();
		this.activationThreshold = this.policy.getParam(Policy.StatType.CYTO_MICROGLIA_THRESHOLD).getEffectiveValue();
		this.infiammatoryState = this.policy.isNLRB3inibitor();
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void perceiveCytokines() {

		 var originalVal = cytokineLayer.get(this.grid.getLocation(this).getX(), this.grid.getLocation(this).getY());
		
		 if(originalVal >= this.activationThreshold) {
			this.infiammatoryState = true;
			this.cytokineRelease();
			return;
		 }
		 else if(originalVal <= 0) {
			 this.infiammatoryState = false;
			 return;
		 }
	}
	
	
	public boolean isInflammated() {
		return this.infiammatoryState;
	}

}
