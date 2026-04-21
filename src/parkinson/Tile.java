package parkinson;

import java.util.concurrent.atomic.AtomicInteger;

import repast.simphony.context.Context;
import repast.simphony.space.SpatialException;
import repast.simphony.space.grid.Grid;



public class Tile extends GridObj implements HasUtility {
	
	private boolean beingPushed;
	private volatile AtomicInteger utility;
	
	public Tile(Context context, Grid<Object> grid)
	{
		super(context, grid);
		this.beingPushed = false;
		this.utility = new AtomicInteger(4);
	}	
	
	public boolean move(GridDirection direction) throws SpatialException
	{
		int newX = this.grid.getLocation(this).getX() + direction.getX();
		int newY = this.grid.getLocation(this).getY() + direction.getY();
		
		return this.move(newX, newY);
	}
	
	public boolean move(int x, int y) throws SpatialException
	{
		
		if(this.grid.getObjectAt(x, y) instanceof Hole) {
			Hole hole = (Hole) this.grid.getObjectAt(x, y);
			context.remove(hole);
			context.remove(this);
			context.add(new Hole(this.context, this.grid));
			return true;
			
		} else {
			return this.grid.moveTo(this, x, y);
		}
	}
	

	public boolean isBeingPushed()
	{
		return this.beingPushed;
	}
	
	public void setBeingPushed()
	{
		this.beingPushed = true;
	}

	// 4. Add atomic operations
	public int decrementUtility() {
		return this.utility.decrementAndGet();
	}

	public int incrementUtility() {
		return this.utility.incrementAndGet();
	}

	@Override
	public synchronized int getUtility() {
		return this.utility.get();
	}

	@Override
	public synchronized void setUtility(int utility) {
		this.utility.getAndSet(utility);
	}

}
