package parkinson.agent.active;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import parkinson.Environment;
import repast.simphony.context.Context;

public class Agent {
	protected Context context;
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	protected Environment env;
	
	public Agent(Context context)
	{
		this.context = context;
		this.context.add(this);
		
		this.space = (ContinuousSpace<Object>) context.getProjection("space");
		this.grid = (Grid<Object>) context.getProjection("grid");
		
		this.env = (Environment) this.context.getObjectsAsStream(Environment.class).findFirst().get();
		
		this.grid.moveTo(this, (int) this.space.getLocation(this).getX(), (int) this.space.getLocation(this).getY());
	}
	
	public void moveTo(double x, double y) {		
    	if(x < 0) x = 0.1;
    	if(y < 0) y = 0.1;
    	double gridDim = this.space.getDimensions().getWidth();
    	if(x >= gridDim) x = gridDim - .1;
    	if(y >= gridDim) y = gridDim - .1;
		
		this.space.moveTo(this, x, y);
		this.grid.moveTo(this, (int) x, (int) y);
	}
}