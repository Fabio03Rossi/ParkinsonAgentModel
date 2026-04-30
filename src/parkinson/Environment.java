package parkinson;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.valueLayer.ValueLayerDiffuser;

public class Environment {
	private ValueLayerDiffuser diffuser;

    public Environment(ValueLayerDiffuser diffuser) {
        this.diffuser = diffuser;
    }
    
    @ScheduledMethod(start = 1, interval = 1)
    public void step() {
        diffuser.diffuse();
    }
}
