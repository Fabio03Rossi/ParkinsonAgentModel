package parkinson;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class Neuron extends Agent {

	private final int MAX_HEALTH;

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridValueLayer cytoValueLayer;
	private GridValueLayer alphaValueLayer;
	
	private NeuronState state;
	
	// Valori del neurone
	private double cytokineValue;
	private double alphaValue;
	private int health;
	
	private final double x;
	private final double y;
	
	private int debris;
	private int alphaSinucleinTreshold;
	private int cytokineTreshold;
	
	private boolean flag;

	private static final Random rnd = new Random();
	
	public Neuron(Context context, int cytokineTreshold, int debris, int alphaSinucleinTreshold, int maxHealth) {
	
		super(context);
		this.space = (ContinuousSpace<Object>) context.getProjection("space");
		this.grid = (Grid<Object>) context.getProjection("grid");
		this.cytoValueLayer = (GridValueLayer) context.getValueLayer("cytoLayer");
		this.alphaValueLayer = (GridValueLayer) context.getValueLayer("alphaLayer");

		this.alphaSinucleinTreshold = alphaSinucleinTreshold;
		this.cytokineTreshold = cytokineTreshold;
		this.state = NeuronState.HEALTHY;
		this.debris = debris;
		
		
		this.x = grid.getLocation(this).getX();
		this.y = grid.getLocation(this).getY();
		
		this.MAX_HEALTH = maxHealth;
		this.health = maxHealth;
	}

	
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void cytokineAbsorption() {
		if(perceiveCytokines()) {
			absorbCytokine();
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 5)
	public void synucleineAbsorption() {
		if(perceiveSynuclein()) {
			absorbSynuclein();
		}
	}
	
    @ScheduledMethod(start = 1, interval = 1, priority = 3)
    public void step1() {
        switch (this.state) {
            case HEALTHY:
            	if(health <= 0) this.state = NeuronState.DEGENERATED_DEATH;
            	if(alphaValue >= alphaSinucleinTreshold || cytokineValue >= cytokineTreshold) this.state = NeuronState.STRESSED;
            		
            break;
                
            case STRESSED:
            	if(alphaValue < alphaSinucleinTreshold && cytokineValue < cytokineTreshold) this.state = NeuronState.HEALTHY;
            	
            	if(this.health > 0){
            		this.loseHealth();
                }
            	else {
                    	this.state = NeuronState.DEGENERATED_DEATH;
                    	System.out.println("Il neurone è morto");
            		}
            	
            break;

            case DEGENERATED_DEATH:
            	if(!flag) {
            		var originalVal = this.alphaValueLayer.get(this.grid.getLocation(this).getX(), this.grid.getLocation(this).getY());
            		
            		// TODO QUI HO MESSO 1 PER TEST, IN REALTA VA RILASCIATA LA QUANTITA DI ALPHA ACCUMULATA
            		this.alphaValueLayer.set(originalVal + 100, this.grid.getLocation(this).getX(), this.grid.getLocation(this).getY());
            		flag = true;
            		System.out.println("Rilascio alpha-sinucleine");
            	}
                break;
        }
        
  
    }
	
	private void absorbCytokine() {
		// TODO Logica per controllo numero neuroni
		int i = 3;
		
		double oldValue = this.cytoValueLayer.get(x, y);
		double absorbedValue = oldValue / i;
		double newValue = oldValue - absorbedValue;
	
 	    cytoValueLayer.set(newValue, (int) this.x, (int) this.y);
 	    this.cytokineValue = this.cytokineValue + absorbedValue;
 	    System.out.println("cytoValueInNeuron: " + cytokineValue);	 
	}


	private void absorbSynuclein() {
				
		//TODO
		int i = 3;
	
		double oldValue = this.alphaValueLayer.get(x, y);
		double absorbedValue = oldValue / i;
		double newValue = oldValue - absorbedValue;
	
 	    alphaValueLayer.set(newValue, (int) this.x, (int) this.y);
 	    this.alphaValue = this.alphaValue + absorbedValue;
 	    System.out.println("alphaValueInNeuron: " + alphaValue);	 
	}
	
	protected boolean perceiveSynuclein() {
		
		// Ottengo la posizione dalla grigli
		alphaValue = this.alphaValueLayer.get(x,y);
		   if(alphaValue > 0) {
			
			   return true;
		   }else return false;
	}
	
	protected boolean perceiveCytokines() {
		
		// Ottengo la posizione dalla griglia
			
		   if(this.cytoValueLayer.get(x,y) > 0) {
			   return true;
		   }else return false;
	}
	
	public void loseHealth() {
		this.health--;
		System.out.println("Health del neurone scesa a: " + this.health);
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
    
	public double getAlphaValue() {
		return alphaValue;
	}
	
	public void setAlphaValue(double alphaValue) {
		this.alphaValue = alphaValue;
	}
	
	public NeuronState getState() {
		return state;
	}

	public void setState(NeuronState state) {
		this.state = state;
	}
	
	public int getMaxHealth() {
		return MAX_HEALTH;
	}
	
	public int getHealth() {
		return health;
	}
	
	public void setCytokineValue(double cytokineValue) {
		this.cytokineValue = cytokineValue;
	}
	
}
