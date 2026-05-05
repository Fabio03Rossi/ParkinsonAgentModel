package parkinson;

import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class GlialCell extends Agent{

	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	protected GlialState state;
	
	
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
		
	}
	
	protected void perceiveCytokines() {
		Iterable within = new ContinuousWithin(this.space, this, 1).query();
		Iterator<Object> t = within.iterator();
		
		while(t.hasNext()) {
			Object c = t.next();
			if(c instanceof Cytokine) {
				cytokinesReceived++;
			}
			
		}
		
		if(cytokinesReceived > activationThreshold) {
			this.state = GlialState.INFLAMMATORY;
		}
		
		
	}

}
