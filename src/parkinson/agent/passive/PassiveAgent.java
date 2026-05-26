package parkinson.agent.passive;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class PassiveAgent {
	protected Context context;
	protected Environment env;
	public PassiveAgent(Context context)
	{
		this.context = context;
		this.context.add(this);
		
		this.env = (Environment) this.context.getObjectsAsStream(Environment.class).findFirst().get();		
	}
}
