package parkinson;

import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class Microglia extends GlialCell{
	private int perceptionRange;
	private GlialState GliaState;

	
	
<<<<<<< HEAD
	public Microglia(Context context, int activationThreshold, int perceptionRange, int cytokineRange, int cytokineReleaseRate) {
		super(context, activationThreshold);
=======
	public Microglia(Context context, ContinuousSpace<Object> space, Grid<Object> grid, int activationThreshold, int perceptionRange, int cytokineRange, int cytokineReleaseRate) {
		super(context, space, grid, activationThreshold, cytokineRange, cytokineReleaseRate);
>>>>>>> 4930f4491b1b94a7b7417ffd0887a48a323ee630
		this.perceptionRange = perceptionRange;
		this.state = GlialState.RESTING;
	}
	
	
	protected Neuron targetNeuron;
	
	
	
	
    @ScheduledMethod(start = 1, interval = 1, priority = 5)
    public void step1() {
        switch (this.state) {
            case RESTING:
                this.perceiveNeurons();

            break;
                
            case DAMAGE_PERCEIVED:
            	// TODO muovere passo passo la cellula gliale
            	this.state = GlialState.PHAGOCITATION;
            break;

            case PHAGOCITATION:
            	this.context.remove(targetNeuron);
            	this.state = GlialState.RESTING;
                break;
        }
       
    }
	
	@Override
	protected void perceiveCytokines() {
		super.perceiveCytokines();
	}
	
	protected void perceiveNeurons() {
		
		
		Iterable within = new ContinuousWithin(this.context, this, cytokineRange).query();
		for(var x : within) {
			if(x instanceof Neuron) {
				Neuron n = (Neuron) x;
				if(n.getState() == NeuronState.DEGENERATED_DEATH) {
					this.state = GlialState.DAMAGE_PERCEIVED;
					this.targetNeuron = n;
				}
			}
		}
		
		
		
	}

}
