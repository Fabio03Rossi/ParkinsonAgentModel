package parkinson;

import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.WrapAroundBorders;

public class ParkinsonBuilder implements ContextBuilder<Object>{
	
	private static final int GRID_MIN_SIZE = 10;
	private static final int GRID_MAX_SIZE = 100;
	private static final int TH_MIN_NUM = 2;
	private static final int TH_MAX_NUM = 100;
	private static final int PLAYER_MIN_NUM = 1;
	private static final int PLAYER_MAX_NUM = 100;
	
	@SuppressWarnings("rawtypes")
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("TileWorld");
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		int gridSize = Math.abs((Integer) params.getValue("grid_size"));
		int thNum = Math.abs((Integer) params.getValue("tiles_holes_num"));
		int plyNum = Math.abs((Integer) params.getValue("player_num"));
		
		gridSize = normalizeValue(gridSize, GRID_MIN_SIZE, GRID_MAX_SIZE);
		thNum = normalizeValue(thNum, TH_MIN_NUM, TH_MAX_NUM);
		plyNum = normalizeValue(plyNum, PLAYER_MIN_NUM, PLAYER_MAX_NUM);
		
		// Grid initialization setup
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(new HashMap());
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				GridBuilderParameters.singleOccupancy2D(
						new LimitedRandomGridAdder<Object>(), new WrapAroundBorders(), gridSize, gridSize)
				);
		
		
		for(int i = 0; i < plyNum; i++) {
			// Objects to add inside of the grid
			new Player(context, grid);
		}

		for(int i = 0; i < thNum; i++) {
			new Tile(context, grid);
			new Hole(context, grid);
		}
		
		RunEnvironment.getInstance().endAt(1000);

		return context;
	}
	
	private int normalizeValue(int value, int min, int max) {
		return value >= min && value <= max ? value : Math.min(Math.max(value, min), max);
	}
}
