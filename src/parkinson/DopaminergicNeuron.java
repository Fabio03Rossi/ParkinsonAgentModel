package parkinson;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
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

	private static final Random rnd = new Random();
	
	public DopaminergicNeuron(
			Context context, ContinuousSpace<Object> space, int cytokineMaxValue, int alphaSinucleinMaxValue, int health) {
		
		super(context);
		this.space = space;
		this.alphaSinucleinMaxValue = alphaSinucleinMaxValue;
		this.cytokineMaxValue = cytokineMaxValue;
		
	}

	
    @ScheduledMethod(start = 1, interval = 1, priority = 5)
    public void step1() {
        switch (this.state) {
            case HEALTHY:
            	int x = rnd.nextInt(2);
            	if(x == 1)
            		alphaSinucleinValue++;
            	

            break;
                
            case STRESSED:
            break;

            case DEGENERATED_DEATH:

                break;
        }
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
