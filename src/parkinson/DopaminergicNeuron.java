package parkinson;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;

public class DopaminergicNeuron extends Agent {

	private ContinuousSpace<Object> space;
	private DopaminergicNeuronState state;
	
	// Valori del neurone
	private int cytokineValue;
	private int alphaSinucleinValue;
	private int health;
	
	private int alphaSinucleinMaxValue;
	private int cytokineMaxValue;
	
	
	public DopaminergicNeuron(
			Context context, ContinuousSpace<Object> space, int cytokineMaxValue, int alphaSinucleinMaxValue, int health) {
		
		super(context);
		this.space = space;
		this.alphaSinucleinMaxValue = alphaSinucleinMaxValue;
		this.cytokineMaxValue = cytokineMaxValue;
		
	}

	public DopaminergicNeuronState getState() {
		return state;
	}

	public void setState(DopaminergicNeuronState state) {
		this.state = state;
	}
	
	private void absorbCytokine(Cytokine cytokine) {
		this.context.remove(cytokine);
		this.setCytokineValue(this.cytokineValue + 1);
	}
	
	public void setCytokineValue(int cytokineValue) {
		this.cytokineValue = cytokineValue;
	}
	
}
