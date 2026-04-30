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
	private boolean infiammatoryState; //true per stato infiammatorio, false per stato non infiammato
	private int cytokineRange;
	private int cytokineReleaseRate;
	
	// LAYERS
	private GridValueLayer cytokineLayer;
	
	
	public Microglia(Context context, ContinuousSpace<Object> space, Grid<Object> grid, int activationThreshold, int perceptionRange, int cytokineRange, int cytokineReleaseRate, GridValueLayer cytokineLayer) {
		super(context, space, activationThreshold, grid);
		this.perceptionRange = perceptionRange;
		this.state = GlialState.RESTING;
		this.infiammatoryState = false;
		this.cytokineRange = cytokineRange;
		this.cytokineReleaseRate = cytokineReleaseRate;
	}
	
	
	protected Neuron targetNeuron;
	
	@ScheduledMethod(start = 1, interval = 3, priority = 4)
	public void cytokineRelease() {
		
		// Ottengo la posizione dalla griglia
 	   int x = this.grid.getLocation(this).getX();
 	   int y = this.grid.getLocation(this).getY();
		
		 // Infiammazione da citochine con intervallo maggiore a 1
	       if(this.infiammatoryState == true){

	    	   

	    	   double cytokineValue = cytokineLayer.get(x,y);
	    	   // Setto il nuovo valore
	    	   cytokineLayer.set(++cytokineValue, x, y);
	       }
	       else{
	    	   // TODO Check per citochine nei dintorni
	    	   
	    	   if(cytokineLayer.get(x,y) >= 1) {
	    		   this.state = GlialState.INFLAMMATORY;
	    	   }
	       }
	}
	
	
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
