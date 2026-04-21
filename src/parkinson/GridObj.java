package parkinson;

import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.context.Context;

public class GridObj {
	protected Grid<Object> grid;
	protected Context context;
	
	public GridObj(Context context, Grid<Object> grid)
	{
		this.grid = grid;
		this.context = context;
		
		this.context.add(this);
	}	
}