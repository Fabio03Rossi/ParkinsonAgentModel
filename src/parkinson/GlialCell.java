package parkinson;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class GlialCell extends Agent{

	protected GlialState state;
	protected boolean infiammatoryState; //true per stato infiammatorio, false per stato non infiammato
	
	protected int cytokinesReceived;
	protected float activationThreshold;
	protected int cytokineRange;
	protected int cytokineReleaseRate;
	// LAYERS
	protected GridValueLayer cytokineLayer;
	
	public GlialCell(Context context) {
		super(context);
		
		this.activationThreshold = (float) this.env.getPolicy().getCytoActivationTreshold();
		this.state = GlialState.RESTING;
		
		this.cytokineRange = (int) this.env.getPolicy().getCytoPerceptionRange();
		this.cytokineReleaseRate = (int) this.env.getPolicy().getCytoReleaseRate();
		
		this.cytokineLayer = (GridValueLayer) context.getValueLayer("cytoLayer");
		this.infiammatoryState = false;

	}
	
	
	public void cytokineRelease() {
		// TODO Da ottimizzare probabilmente
		// Ottengo la posizione dalla griglia
		   int x = this.grid.getLocation(this).getX();
		   int y = this.grid.getLocation(this).getY();
	    	   double cytokineValue = cytokineLayer.get(x,y);
	    	   // Setto il nuovo valore
	    	   cytokineLayer.set(cytokineValue + 1, x, y);
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
