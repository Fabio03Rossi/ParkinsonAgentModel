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
	private boolean infiammatoryState; //true per stato infiammatorio, false per stato non infiammato

	
	protected int cytokinesReceived;
	protected int activationThreshold;
	protected int cytokineRange;
	protected int cytokineReleaseRate;
	// LAYERS
	protected GridValueLayer cytokineLayer;
	
	public GlialCell(Context context, ContinuousSpace<Object> space, Grid<Object> grid, int activationThreshold, int cytokineRange, int cytokineReleaseRate) {
		super(context);
		this.space = space;
		this.grid = grid;
		
		this.activationThreshold = activationThreshold;
		this.state = GlialState.RESTING;
		
		this.cytokineRange = cytokineRange;
		this.cytokineReleaseRate = cytokineReleaseRate;
		
		this.cytokineLayer = (GridValueLayer) context.getValueLayer("cytoLayer");
		this.infiammatoryState = false;

	}
	
	
	@ScheduledMethod(start = 1, interval = 3, priority = 4)
	public void cytokineRelease() {
		
		// Ottengo la posizione dalla griglia
		   int x = this.grid.getLocation(this).getX();
		   int y = this.grid.getLocation(this).getY();
		// Se si trova in uno stato infiammatorio rilascia citochine
	       if(this.infiammatoryState == true){
	    	   double cytokineValue = cytokineLayer.get(x,y);
	    	   // Setto il nuovo valore
	    	   cytokineLayer.set(++cytokineValue, x, y);
	    	   System.out.println("cytoValue: " + cytokineValue);	   
	       }else{
	    	   perceiveCytokines(x,y);
	       }
		
		 
	}
	
	protected void perceiveCytokines(int x, int y) {
		
		   if(cytokineLayer.get(x,y) >= 1) {
    		   this.state = GlialState.INFLAMMATORY;
    	   }
	}
		

}
