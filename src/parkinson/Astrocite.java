package parkinson;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class Astrocite extends GlialCell{

	private int perceptionRange;
	private GlialState GliaState;
	private boolean infiammatoryState; //true per stato infiammatorio, false per stato non infiammato
	private double alphaSynAbsorbRatio = 0.1;
	
	private GridValueLayer alphaValueLayer;
	
	protected Neuron targetNeuron;
	
	public Astrocite(Context context, int activationThreshold, int perceptionRange, int cytokineRange, int cytokineReleaseRate) {
		super(context, activationThreshold, cytokineRange, cytokineReleaseRate);
		this.perceptionRange = perceptionRange;
		this.state = GlialState.RESTING;
		this.infiammatoryState = false;
		
		this.alphaValueLayer = (GridValueLayer) context.getValueLayer("alphaLayer");
	}
}
