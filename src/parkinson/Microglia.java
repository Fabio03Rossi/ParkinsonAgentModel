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
	private double alphaSynAbsorbRatio = 0.1;
	
	private GridValueLayer alphaValueLayer;
	
	protected Neuron targetNeuron;
	
	public Microglia(Context context, int activationThreshold, int perceptionRange, int cytokineRange, int cytokineReleaseRate) {
		super(context, activationThreshold, cytokineRange, cytokineReleaseRate);
		this.perceptionRange = perceptionRange;
		this.state = GlialState.RESTING;
		this.infiammatoryState = false;
		
		this.alphaValueLayer = (GridValueLayer) context.getValueLayer("alphaLayer");
	}
	
    @ScheduledMethod(start = 1, interval = 1, priority = 5)
    public void step1() {
        switch (this.state) {
            case RESTING:
                this.perceiveNeurons();
            break;
                
            case DAMAGE_PERCEIVED:
            	this.linkToNeuron();
            	this.state = GlialState.PHAGOCITATION;
            break;

            case PHAGOCITATION:
            	System.out.print("Microglia in fagocitosi");
            	this.absorbAlphaSyn();
            	//this.phagocitation();
            	this.infiammatoryState = true;
                break;
            default:
            	break;
        }
       
    }
    
    protected void linkToNeuron() {
    	var ret = this.space.moveTo(this, this.space.getLocation(targetNeuron).getX() + 1, this.space.getLocation(targetNeuron).getY() + 1);
    	System.out.println(ret);
    }
    
    protected void phagocitation() {
    	var originalVal = this.alphaValueLayer.get(this.grid.getLocation(targetNeuron).getX(), this.grid.getLocation(targetNeuron).getY());
    	if(originalVal <= 0) {
    		this.context.remove(targetNeuron);
        	this.state = GlialState.RESTING;
    	}
    }
    
    protected void absorbAlphaSyn() {
		var originalVal = this.alphaValueLayer.get(this.grid.getLocation(targetNeuron).getX(), this.grid.getLocation(targetNeuron).getY());
    	this.alphaValueLayer.set(originalVal - (0.1 * alphaSynAbsorbRatio), this.grid.getLocation(this).getX(), this.grid.getLocation(this).getY());
    }
	
	protected void perceiveNeurons() {
	
		Iterable within = new ContinuousWithin(this.context, this, 10).query();
		for(var x : within) {
			
			if(x instanceof Neuron) {
				Neuron n = (Neuron) x;
				
				if(n.getState() == NeuronState.DEGENERATED_DEATH) {
					System.out.println(n.getState());
					this.state = GlialState.DAMAGE_PERCEIVED;
					this.targetNeuron = n;
				}
			}
		}
	}

}
