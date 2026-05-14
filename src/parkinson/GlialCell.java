package parkinson;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class GlialCell extends Agent{

	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	protected GlialState state;
	protected boolean infiammatoryState; //true per stato infiammatorio, false per stato non infiammato

	
	protected int cytokinesReceived;
	protected float activationThreshold;
	protected int cytokineRange;
	protected int cytokineReleaseRate;
	// LAYERS
	protected GridValueLayer cytokineLayer;
	
	public GlialCell(Context context, float activationThreshold, int cytokineRange, int cytokineReleaseRate) {
		super(context);
		this.space = (ContinuousSpace<Object>) context.getProjection("space");
		this.grid = (Grid<Object>) context.getProjection("grid");
		
		this.activationThreshold = activationThreshold;
		this.state = GlialState.RESTING;
		
		this.cytokineRange = cytokineRange;
		this.cytokineReleaseRate = cytokineReleaseRate;
		
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
