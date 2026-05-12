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
		var tr = ((Grid) this.context.getProjection("grid")).moveTo(this, (int) loc.getX(), (int) loc.getY());
	}
	
	public void moveTo(double x, double y) {
		var space = ((ContinuousSpace) this.context.getProjection("space"));
		
    	if(x < 0) x = 0.1;
    	if(y < 0) y = 0.1;
    	double gridDim = space.getDimensions().getWidth();
    	if(x >= gridDim) x = gridDim - .1;
    	if(y >= gridDim) y = gridDim - .1;
		
		((ContinuousSpace) this.context.getProjection("space")).moveTo(this, x, y);
		((Grid) this.context.getProjection("grid")).moveTo(this, (int) x, (int) y);
	}
}