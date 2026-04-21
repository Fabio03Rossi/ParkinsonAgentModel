package parkinson;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridAdder;
import repast.simphony.space.grid.GridDimensions;
import simphony.util.messages.MessageCenter;

public class LimitedRandomGridAdder<T> implements GridAdder<T> {

	  private static final int TRY_WARN_LIMIT = 10000;

	  /**
	   * Adds the specified object to the space at a random location.
	   *
	   * @param space the space to add the object to.
	   * @param obj   the object to add.
	   */
	  public void add(Grid<T> space, T obj) {
	    GridDimensions dims = space.getDimensions();
	    int[] location = new int[dims.size()];
	    findLocation(location, dims);
	    int tries = 0;
	    while (!space.moveTo(obj, location)) {
	      findLocation(location, dims);
	      tries++;
	      if (tries == TRY_WARN_LIMIT) {
	        MessageCenter.getMessageCenter(this.getClass()).warn("Possible hang in filling grid '" +
	                space.getName() + "': grid may be full. Please reduce number of agents added, use a larger grid, or " +
	                "write a custom GridAdder");
	      }
	    }
	  }

	  private void findLocation(int[] location, GridDimensions dims) {
	    int[] origin = dims.originToIntArray(null);
	    for (int i = 0; i < location.length; i++) {
	      location[i] = RandomHelper.getUniform().nextIntFromTo(2, dims.getDimension(i) - origin[i] - 3);
	    }
	  }
	}

