package parkinson;

import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;

public class Microglia extends GlialCell{
	private double range;
	
	public Microglia(Context context, ContinuousSpace<Object> space, int activationThreshold, double range ) {
		super(context, space, activationThreshold);
		this.range = range;
	}
	
	protected DopaminergicNeuron targetNeuron;
	
    @ScheduledMethod(start = 1, interval = 1, priority = 5)
    public void step1() {
        switch (this.state) {
            case RESTING:
                this.perceiveCytokines();
                this.perceiveNeurons();

            break;
                
            case DAMAGE_PERCEIVED:
            	this.context.remove(targetNeuron);
            break;

            case PHAGOCITATION:

                break;

            case INFLAMMATORY:

                break;
        }
    }
	
	@Override
	protected void perceiveCytokines() {
		super.perceiveCytokines();
	}
	
	protected void perceiveNeurons() {
		
		
		Iterable within = new ContinuousWithin(this.context, this, range).query();
		Iterator<Object> t = within.iterator();
		for(var x : within) {
			System.out.println(x.toString());
		}
		
		while(t.hasNext()) {
			Object c = t.next();
			if(c instanceof DopaminergicNeuron) {
				DopaminergicNeuron n = (DopaminergicNeuron) c;
				System.out.println("ciao");
				if(n.getState() == DopaminergicNeuronState.DEGENERATED_DEATH) {
					this.state = GlialState.DAMAGE_PERCEIVED;
					this.targetNeuron = n;
					System.out.println("posizione Neurone: " + this.space.getLocation(n));
					System.out.println("posizione glial: " + this.space.getLocation(this));

				}
			}
			
		}
		
		
	}

}
