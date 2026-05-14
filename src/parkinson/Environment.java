package parkinson;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.valueLayer.ValueLayerDiffuser;

public class Environment {
	
	private Policy policy;
	
	private ValueLayerDiffuser cytoDiffuser;
	private ValueLayerDiffuser alphaDiffuser;

    public Environment(Policy policy, ValueLayerDiffuser cytoDiffuser, ValueLayerDiffuser alphaDiffuser) {
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
    
    public Policy getPolicy() {
    	return this.policy;
    }
    
    public ValueLayerDiffuser getCytokineDiffuser() {
    	return cytoDiffuser;
    }
    
    public ValueLayerDiffuser getAlphaDiffuser() {
    	return alphaDiffuser;
    }
}
