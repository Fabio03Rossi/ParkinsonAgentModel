package parkinson;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.valueLayer.ValueLayerDiffuser;

public class Environment {
	
	private ValueLayerDiffuser cytoDiffuser;
	private ValueLayerDiffuser alphaDiffuser;
	private Policy policy;

    public Environment(ValueLayerDiffuser cytoDiffuser, ValueLayerDiffuser alphaDiffuser, Policy policy) {
        this.cytoDiffuser = cytoDiffuser;
        this.alphaDiffuser = alphaDiffuser;
        this.policy = policy;
    }
    
    @ScheduledMethod(start = 1, interval = 1)
    public void step() {
    	cytoDiffuser.diffuse();
    	alphaDiffuser.diffuse();
    }
    
    public void setDiffusionRate(ValueLayerDiffuser v, double diffusionRate) {
    	v.setDiffusionConst(diffusionRate);
    }
    
    public void setEvaporationRate(ValueLayerDiffuser v, double evapRate) {
    	v.setEvaporationConst(evapRate);
    }
    
    public ValueLayerDiffuser getCytokineDiffuser() {
    	return cytoDiffuser;
    }
    
    public ValueLayerDiffuser getAlphaDiffuser() {
    	return alphaDiffuser;
    }
    
    public Policy getPolicy() {
		return policy;
	}
}
