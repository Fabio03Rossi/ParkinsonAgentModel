package parkinson;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;

public class DopaminergicNeuron extends Agent {

	private ContinuousSpace<Object> space;
	private DopaminergicNeuronState state;
	
	public DopaminergicNeuron(Context context, ContinuousSpace<Object> space) {
		super(context);
		this.space = space;
	}

	public DopaminergicNeuronState getState() {
		return state;
	}

	public void setState(DopaminergicNeuronState state) {
		this.state = state;
	}
}
