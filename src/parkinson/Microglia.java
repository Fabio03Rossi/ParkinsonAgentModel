package parkinson;

import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;

public class Microglia extends Agent{

	private ContinuousSpace<Object> space;
	private MicrogliaState state;
	
	private int cytokinesReceived;
	private int activationThreshold;
	
	public Microglia(Context context, ContinuousSpace<Object> space, int activationThreshold) {
		super(context);
		this.space = space;
		this.activationThreshold = activationThreshold;
	}
	
	public void perceive() {
		var within = new ContinuousWithin(this.context, this, 1).query();
	}
	

}
