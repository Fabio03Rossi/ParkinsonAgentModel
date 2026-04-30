package parkinson;

import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;

public class Microglia extends GlialCell{
	private double range;
	private GlialState GliaState;
	private boolean infiammatoryState; //true per stato infiammatorio, false per stato non infiammato
	private double cytokineRange;
	
	
	public Microglia(Context context, ContinuousSpace<Object> space, int activationThreshold, double range, double cytokineRange ) {
		super(context, space, activationThreshold);
		this.range = range;
		this.state = GlialState.RESTING;
		this.infiammatoryState = false;
		this.cytokineRange = cytokineRange;
	}
	
	
	ValueLayerDiffuser 
	
	
	protected Neuron targetNeuron;
	
    @ScheduledMethod(start = 1, interval = 1, priority = 5)
    public void step1() {
        switch (this.state) {
            case RESTING:
                this.perceiveCytokines();
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
        
        
        // Infiammazione da citochine
       if(this.infiammatoryState == true){
    	   // TODO Produce citochine
       }
       else{
    	   // TODO Check per citochine nei dintorni
    	   Iterable within = new ContinuousWithin(this.context, this, cytokineRange).query();
       }
    }
	
	@Override
	protected void perceiveCytokines() {
		super.perceiveCytokines();
	}
	
	protected void perceiveNeurons() {
		
		
		Iterable within = new ContinuousWithin(this.context, this, range).query();
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
