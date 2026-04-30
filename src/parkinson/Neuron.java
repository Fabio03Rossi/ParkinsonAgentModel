package parkinson;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;

public class Neuron extends Agent {

	private ContinuousSpace<Object> space;
	private NeuronState state;
	
	// Valori del neurone
	private int cytokineValue;
	private int alphaSinucleinValue;
	private int health;
	
	private int debris;
	private int alphaSinucleinTreshold;
	private int cytokineTreshold;

	private static final Random rnd = new Random();
	
	public Neuron(
			Context context, ContinuousSpace<Object> space, int cytokineTreshold, int debris, int alphaSinucleinTreshold, int health) {
		
		super(context);
		this.space = space;
		this.alphaSinucleinTreshold = alphaSinucleinTreshold;
		this.cytokineTreshold = cytokineTreshold;
		this.state = NeuronState.HEALTHY;
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
        
        if(alphaSinucleinValue >= alphaSinucleinTreshold)
		{
        	if(this.health != 0)
            {
        		this.loseHealth();
            }
        	else 
        	{
        		if(this.state != NeuronState.DEGENERATED_DEATH) {
                	this.state = NeuronState.DEGENERATED_DEATH;
                	System.out.println("Il neurone è morto");
        		}
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
    
	public NeuronState getState() {
		return state;
	}

	public void setState(NeuronState state) {
		this.state = state;
	}
	
	
	public void setCytokineValue(int cytokineValue) {
		this.cytokineValue = cytokineValue;
	}
	
}
