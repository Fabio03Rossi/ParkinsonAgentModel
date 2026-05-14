package parkinson;

import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class Microglia extends GlialCell {
	private int perceptionRange;
	private GlialState GliaState;
	private double alphaSynAbsorbRatio = 0.1;
	
	private double xToReach = -1;
	private double yToReach = -1;
	
	private GridValueLayer alphaValueLayer;
	
	protected Neuron targetNeuron;
	
	public Microglia(Context context, float activationThreshold, int perceptionRange, int cytokineRange, int cytokineReleaseRate) {
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
            	var gLoc = this.grid.getLocation(this);
            	var sLoc = this.space.getLocation(this);

            	if(gLoc.getX() != (int) xToReach || gLoc.getY() != (int) yToReach) { 		
            		float xDif = 0, yDif = 0;
            		
            		if(sLoc.getX() > xToReach) xDif -= .1f;
            		if(sLoc.getX() < xToReach) xDif += .1f;
            		if(sLoc.getY() > yToReach) yDif -= .1f;
            		if(sLoc.getY() < yToReach) yDif += .1f;
            		
                	this.moveTo(sLoc.getX() + xDif, sLoc.getY() + yDif);
            	} else {
            		this.state = GlialState.PHAGOCITATION;
            	}
            break;

            case PHAGOCITATION:
            	this.phagocitation();
            	this.cytokineRelease();
            	this.infiammatoryState = true;
                break;
            default:
            	break;
        }
       
    }
    

	
	
    protected void linkToNeuron() {
    	if(!context.contains(targetNeuron)) {
    		this.state = GlialState.RESTING;
    		return;
    	}
    	double rndX = Math.random();
    	double rndY = Math.random();
    	
    	double newX = this.space.getLocation(targetNeuron).getX() + rndX;
    	double newY = this.space.getLocation(targetNeuron).getY() + rndY;
    	
    	if(newX < 0) newX = 0.1;
    	if(newY < 0) newY = 0.1;
    	double gridDim = this.space.getDimensions().getWidth();
    	if(newX >= gridDim) newX = gridDim -0.1;
    	if(newY >= gridDim) newY = gridDim -0.1;
    	
    	xToReach = newX;
    	yToReach = newY;
    	//this.space.moveTo(this, newX, newY);
    	//this.grid.moveTo(this, (int) newX, (int) newY);
    }
    
    protected void phagocitation() {
    		this.context.remove(targetNeuron);
        	this.state = GlialState.RESTING;
    }
    
    protected void absorbAlphaSyn() {
		var originalVal = this.alphaValueLayer.get(this.grid.getLocation(targetNeuron).getX(), this.grid.getLocation(targetNeuron).getY());
    	this.alphaValueLayer.set(originalVal - (0.1 * alphaSynAbsorbRatio), this.grid.getLocation(this).getX(), this.grid.getLocation(this).getY());
    }
	
	protected void perceiveNeurons() {
		this.targetNeuron = null;
		
		Iterable within = new ContinuousWithin(this.context, this, 8).query();
		for(var x : within) {
			
			if(x instanceof Neuron) {
				Neuron n = (Neuron) x;
				
				if(n.getState() == NeuronState.DEGENERATED_DEATH) {
					System.out.println("Identificato un neurone in stato DEGENERATED_DEATH");
					this.state = GlialState.DAMAGE_PERCEIVED;
					this.targetNeuron = n;
				}
				
			}
		}
	}
	
	public GlialState getState() {
		return this.state;
	}
}
