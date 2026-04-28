package parkinson;

import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;

public class GlialCell extends Agent{

	protected ContinuousSpace<Object> space;
	protected GlialState state;
	
	protected int cytokinesReceived;
	protected int activationThreshold;
	
	public GlialCell(Context context, ContinuousSpace<Object> space, int activationThreshold) {
		super(context);
		this.space = space;
		this.activationThreshold = activationThreshold;
		this.state = GlialState.RESTING;
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
