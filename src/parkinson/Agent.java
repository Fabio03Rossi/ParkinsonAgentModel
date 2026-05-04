package parkinson;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.context.Context;

public class Agent {
	protected Context context;
	
	public Agent(Context context)
	{
		this.context = context;
		this.context.add(this);
		
		var loc = ((ContinuousSpace) this.context.getProjection("space")).getLocation(this);
		((Grid) this.context.getProjection("grid")).moveTo(this, (int) loc.getX(), (int) loc.getY());
	}
}