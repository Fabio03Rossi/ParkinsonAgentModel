package parkinson;

import parkinson.agent.active.Microglia;
import parkinson.agent.active.Neuron;
import parkinson.agent.passive.Treatment;
import parkinson.learning.TreatmentModel;
import parkinson.utils.NeuronState;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunListener;
import repast.simphony.essentials.RepastEssentials;
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
		//String condition = (String) params.getValue("condition");
		
		// Treatment
		//String treatment = (String) params.getValue("treatment");
		
		// General
		int spaceSize = Math.abs((Integer) params.getValue("space_size"));
		int microNum = Math.abs((Integer) params.getValue("micro_num"));
		int astroNum = Math.abs((Integer) params.getValue("astro_num"));
		int neuroNum = Math.abs((Integer) params.getValue("neuro_num"));

		int neuronHealth = Math.abs((Integer) params.getValue("neuron_health"));
		float cytoMicrogliaThr = Math.abs((Float) params.getValue("cyto_microglia_threshold"));
		float cytoRate = Math.abs((Float) params.getValue("cytokine_release_rate"));
		float alphaThr = Math.abs((Float) params.getValue("alpha_threshold"));
		float cytoThr = Math.abs((Float) params.getValue("cyto_neuron_threshold"));
		float neuronDegenRate = Math.abs((Float) params.getValue("neuron_degeneration_rate"));
		int diffusionConstant = 1;
		int diffusionEvaporation = 1;
		
		Policy policy = Policy.createInstance(70, true, null, cytoMicrogliaThr, cytoRate, alphaThr, cytoThr, neuronDegenRate);
		
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
		
		Environment env = new Environment(cytoDiffuser, alphaDiffuser);
		context.add(env);
		
		/* 		
		for(int i = 0; i < neuroNum; i++) {
			new Neuron(context, neuronHealth);
		}

		var x = new Neuron(context, neuronHealth);
		x.setHealth(0);
		alphaLayer.set(10, grid.getLocation(x).getX(), grid.getLocation(x).getY());
		*/
		initializeEpisodeState(context, grid, alphaLayer, neuroNum, neuronHealth);
		for(int i = 0; i < microNum; i++) {
			new Microglia(context);
		}
		
		new Treatment(context);
		
		RunEnvironment.getInstance().endAt(1200);
		
		return context;
	}

	private void initializeEpisodeState(Context<Object> context, Grid<Object> grid, GridValueLayer alphaLayer, int neuroNum, int neuronHealth) {
		int currentRun = Treatment.getBatchRunNumber();
		
		// Curriculum: aumenta difficoltà ogni N episodi
		int difficulty = (int) (currentRun / 50);  // ogni 50 run, step avanti
		
		int initialDegCount, initialStressCount, initialInflamCount;
		
		switch(difficulty % 4) {
			case 0: // Easy: no initial inflammation
					initialDegCount = 1;
					initialStressCount = 0;
					initialInflamCount = 0;
					break;
			case 1: // Medium: baseline inflammation
					initialDegCount = 2;
					initialStressCount = 3;
					initialInflamCount = 2;
					break;
			case 2: // Hard: propagated inflammation
					initialDegCount = 4;
					initialStressCount = 5;
					initialInflamCount = 4;
					break;
			case 3: // Extreme: high inflammation (test robustness)
					initialDegCount = 8;
					initialStressCount = 10;
					initialInflamCount = 8;
					break;
			default:
					initialDegCount = 1;
					initialStressCount = 0;
					initialInflamCount = 0;
		}
    
		// Leggi il batch run number
		int batchRun = Treatment.getBatchRunNumber();

		// Crea neuroni healthy
		int healthyToCreate = neuroNum - initialDegCount - initialStressCount;
		for(int i = 0; i < healthyToCreate; i++) {
			new Neuron(context, neuronHealth);
		}

		// Aggiungi neuroni MORTI
		for(int i = 0; i < initialDegCount; i++) {
			var deadNeuron = new Neuron(context, neuronHealth);
			deadNeuron.setHealth(0);  // NeuronState.DEGENERATED_DEATH
			alphaLayer.set(10, grid.getLocation(deadNeuron).getX(), grid.getLocation(deadNeuron).getY());
		}

		// Aggiungi neuroni STRESSATI
		for(int i = 0; i < initialStressCount; i++) {
			var stressedNeuron = new Neuron(context, neuronHealth);
			stressedNeuron.setHealth(neuronHealth - 10);  // ~30% salute = STRESSED
			stressedNeuron.setState(NeuronState.STRESSED);
		}
	}
}
