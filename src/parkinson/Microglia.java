package parkinson;

import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;

public class Microglia extends GlialCell{

	public Microglia(Context context, ContinuousSpace<Object> space, int activationThreshold) {
		super(context, space, activationThreshold);

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
		Iterable within = new ContinuousWithin(this.space, this, 1).query();
		Iterator<Object> t = within.iterator();
		
		while(t.hasNext()) {
			Object c = t.next();
			if(c instanceof DopaminergicNeuron) {
				DopaminergicNeuron n = (DopaminergicNeuron) c;
				if(n.getState() == DopaminergicNeuronState.DEGENERATED_DEATH) {
					this.state = GlialState.DAMAGE_PERCEIVED;
					this.targetNeuron = n;
				}
			}
			
		}
		
		
	}

}
