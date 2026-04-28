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
	
	private int debris;
	private int alphaSinucleinMaxValue;
	private int cytokineMaxValue;

	private static final Random rnd = new Random();
	
	public DopaminergicNeuron(
			Context context, ContinuousSpace<Object> space, int cytokineMaxValue, int debris, int alphaSinucleinMaxValue, int health) {
		
		super(context);
		this.space = space;
		this.alphaSinucleinMaxValue = alphaSinucleinMaxValue;
		this.cytokineMaxValue = cytokineMaxValue;
		this.state = DopaminergicNeuronState.HEALTHY;
		this.debris = debris;
		
	}

	
    @ScheduledMethod(start = 1, interval = 1, priority = 5)
    public void step1() {
        switch (this.state) {
            case HEALTHY:
            	int x = rnd.nextInt(2);
            	if(x == 1)
            	{
            		alphaSinucleinValue++;
            		System.out.println("Alpha sinucleina aggiunta al neurone");
            	}
            	

            break;
                
            case STRESSED:
            break;

            case DEGENERATED_DEATH:

                break;
        }
        
        
        
        // CONTROLLI DI STEP
        
        if(alphaSinucleinValue >= alphaSinucleinMaxValue)
		{
        	if(this.health == 0)
            {
            	this.state = DopaminergicNeuronState.DEGENERATED_DEATH;
            	System.out.println("Il neurone è morto");
            }
        	else 
        	{
    			this.loseHealth();
        	}
		}
        
    }
	
	private void absorbCytokine(Cytokine cytokine) {
		this.context.remove(cytokine);
		this.setCytokineValue(this.cytokineValue + 1);
	}
	
	public void loseHealth() {
		this.health--;
		System.out.println("Health del neurone scesa a: " + this.health);
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
    
	public DopaminergicNeuronState getState() {
		return state;
	}

	public void setState(DopaminergicNeuronState state) {
		this.state = state;
	}
	
	
	public void setCytokineValue(int cytokineValue) {
		this.cytokineValue = cytokineValue;
	}
	
}
