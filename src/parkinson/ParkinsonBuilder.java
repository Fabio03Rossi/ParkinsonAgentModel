package parkinson;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import repast.simphony.parameter.Parameters;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;

public class ParkinsonBuilder implements ContextBuilder<Object>{

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("Parkinson");
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		// Patient 
		//boolean gender = (Boolean) params.getValue("gender");
		//int age = Math.abs((Integer) params.getValue("age"));
		
		
		
		// Treatment
		
		
		// General
		int spaceSize = Math.abs((Integer) params.getValue("space_size"));
		int microNum = Math.abs((Integer) params.getValue("micro_num"));
		int astroNum = Math.abs((Integer) params.getValue("astro_num"));
		int neuroNum = Math.abs((Integer) params.getValue("neuro_num"));
		int perceptionRange = Math.abs((Integer) params.getValue("range"));
		int cytoRange = Math.abs((Integer) params.getValue("cytokine_range"));

		int neuroHealth = Math.abs((Integer) params.getValue("neuro_health"));
		float actThr = Math.abs((Float) params.getValue("activation_threshold"));
		int cytoThr = Math.abs((Integer) params.getValue("cytokines_threshold"));
		int alphaThr = Math.abs((Integer) params.getValue("alpha_threshold"));
		int debris = Math.abs((Integer) params.getValue("debris_released"));
		int cytoRate = Math.abs((Integer) params.getValue("cytokines_released"));
		int diffusionConstant = 1;
		int diffusionEvaporation = 1;
		
		
		double debrisStr = Math.abs((Double) params.getValue("debris_strength"));
		double cytoStr = Math.abs((Double) params.getValue("cytokines_strength"));
		
		
		Policy policy = new Policy(70, true, null, actThr, alphaThr, cytoThr);
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.BouncyBorders(), spaceSize,
				spaceSize);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid(
				"grid", context, 
				GridBuilderParameters.multiOccupancy2D(new SimpleGridAdder<Object>(), new StrictBorders(), spaceSize, spaceSize)
		);
		
		GridValueLayer cytoLayer = new GridValueLayer(
				"cytoLayer", 0.0, false, new StrictBorders(), spaceSize, spaceSize  
		);
		context.addValueLayer(cytoLayer);
		
		GridValueLayer alphaLayer = new GridValueLayer(
				"alphaLayer", 0.0, false, new StrictBorders(), spaceSize, spaceSize  
		);
		context.addValueLayer(alphaLayer);
		
		ValueLayerDiffuser cytoDiffuser = new ValueLayerDiffuser(cytoLayer, 1.0, 1.0, false);
		// Impostazioni diffuserLayer
		cytoDiffuser.setDiffusionConst(diffusionConstant); 		// 1 = [0, 10, 0] gives [5, 0, 5].
		cytoDiffuser.setEvaporationConst(diffusionEvaporation); 	// 1 = no evaporation
		cytoDiffuser.setMinValue(0f);
		cytoDiffuser.setMaxValue(1.0f);
		
		ValueLayerDiffuser alphaDiffuser = new ValueLayerDiffuser(alphaLayer, 1.0, 1.0, false);
		
		// Impostazioni diffuserLayer
		alphaDiffuser.setDiffusionConst(diffusionConstant); 		// 1 = [0, 10, 0] gives [5, 0, 5].
		alphaDiffuser.setEvaporationConst(diffusionEvaporation); 	// 1 = no evaporation
		alphaDiffuser.setMinValue(0f);
		alphaDiffuser.setMaxValue(1.0f);
		
		context.add(new Environment(policy, cytoDiffuser, alphaDiffuser));
				
		for(int i = 0; i < neuroNum; i++) {
			new Neuron(context, cytoThr, debris, alphaThr, neuroHealth);
		}
		
		var x = new Neuron(context, cytoThr, debris, alphaThr, neuroHealth);
		x.setHealth(0);
		alphaLayer.set(10, grid.getLocation(x).getX(), grid.getLocation(x).getY());
		
		for(int i = 0; i < microNum; i++) {
			new Microglia(context, actThr, perceptionRange, cytoRange, cytoRate);
		}
		
		RunEnvironment.getInstance().endAt(800);

		return context;
	}
}
