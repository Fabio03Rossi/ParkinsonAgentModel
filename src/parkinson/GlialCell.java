package parkinson;

import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class GlialCell extends Agent{

	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	protected GlialState state;
	protected boolean infiammatoryState; //true per stato infiammatorio, false per stato non infiammato

	
	protected int cytokinesReceived;
	protected int activationThreshold;
	protected int cytokineRange;
	protected int cytokineReleaseRate;
	// LAYERS
	protected GridValueLayer cytokineLayer;
	
	public GlialCell(Context context, int activationThreshold, int cytokineRange, int cytokineReleaseRate) {
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
	    	   //System.out.println("cytoValue: " + cytokineValue);	    
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void perceiveCytokines() {

		 var originalVal = cytokineLayer.get(this.grid.getLocation(this).getX(), this.grid.getLocation(this).getY());
		
		 if(originalVal >= 0.1) {
			this.infiammatoryState = true;
			//System.out.println("CITOCHINE PERCEPITE, RILASCIO CITOCHINE");
			cytokineRelease();
			return;
		 }
		 else if(originalVal <= 0) {
			 this.infiammatoryState = false;
			 return;
		 }
	}
		

}
