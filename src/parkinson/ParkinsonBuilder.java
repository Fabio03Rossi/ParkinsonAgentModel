package parkinson;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.BouncyBorders;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.parameter.Parameters;

public class ParkinsonBuilder implements ContextBuilder<Object>{

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("Parkinson");
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		int spaceSize = Math.abs((Integer) params.getValue("space_size"));
		int microNum = Math.abs((Integer) params.getValue("micro_num"));
		int astroNum = Math.abs((Integer) params.getValue("astro_num"));
		int neuroNum = Math.abs((Integer) params.getValue("neuro_num"));

		int range = Math.abs((Integer) params.getValue("range"));
		int neuroHealth = Math.abs((Integer) params.getValue("neuro_health"));
		int actThr = Math.abs((Integer) params.getValue("activation_threshold"));
		int debris = Math.abs((Integer) params.getValue("debris_released"));
		int cyto = Math.abs((Integer) params.getValue("cytokines_released"));
		//int alphaSinucleinLimit = Math.abs((Integer) params.getValue("alphaSinucleinLimit"));
		int alphaSinucleinLimit = 1;
		
		
		double debrisStr = Math.abs((Double) params.getValue("debris_strength"));
		double cytoStr = Math.abs((Double) params.getValue("cytokines_strength"));
		
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new BouncyBorders(), spaceSize,
				spaceSize);
		
		for(int i = 0; i < neuroNum; i++) {
			new Neuron(context, space, cyto, debris, alphaSinucleinLimit, neuroHealth);
		}
		
		for(int i = 0; i < microNum; i++) {
			new Microglia(context, space, actThr, range);
		}
		
		RunEnvironment.getInstance().endAt(1000);

		return context;
	}
}
