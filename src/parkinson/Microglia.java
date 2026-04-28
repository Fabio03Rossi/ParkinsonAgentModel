package parkinson;

import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;

public class Microglia extends GlialCell{

	public Microglia(Context context, ContinuousSpace<Object> space, int activationThreshold) {
		super(context, space, activationThreshold);
		// TODO Auto-generated constructor stub
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
				}
			}
			
		}
		
		
	}

}
