package parkinson;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.WrapAroundBorders;

public class ParkinsonBuilder implements ContextBuilder<Object>{

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("Parkinson");
		
		//Parameters params = RunEnvironment.getInstance().getParameters();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new WrapAroundBorders(), 50,
				50);
		
		RunEnvironment.getInstance().endAt(1000);

		return context;
	}
}
