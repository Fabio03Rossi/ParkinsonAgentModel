package parkinson;

import java.util.Random;

import parkinson.Policy.StatType;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class Neuron extends Agent {

	private final int MAX_HEALTH;

	private GridValueLayer cytoValueLayer;
	private GridValueLayer alphaValueLayer;
	
	private NeuronState state;
	
	// Valori del neurone
	private double cytokineValue;
	private double alphaValue;
	private double health;
	
	private double x;
	private double y;
	
	private double degenerationRate;
	private double alphaSinucleinThreshold;
	private double cytokineThreshold;
	
	private boolean flag;

	private Policy policy;	
	
	
	@SuppressWarnings("unchecked")
	public Neuron(Context<Object> context, int maxHealth) {
		super(context);
		
		this.policy = Policy.getInstance();
		this.space = (ContinuousSpace<Object>) context.getProjection("space");
		this.grid = (Grid<Object>) context.getProjection("grid");

		this.cytoValueLayer = (GridValueLayer) context.getValueLayer("cytoLayer");
		this.alphaValueLayer = (GridValueLayer) context.getValueLayer("alphaLayer");

		this.alphaSinucleinThreshold = this.policy.getParam(Policy.StatType.ALPHA_SINUCLEIN_THRESHOLD).getEffectiveValue();
		this.cytokineThreshold = this.policy.getParam(Policy.StatType.CYTO_NEURON_THRESHOLD).getEffectiveValue();
		this.degenerationRate = this.policy.getParam(Policy.StatType.DEGENERATION_RATE).getEffectiveValue();

		this.state = NeuronState.HEALTHY;
		
		this.x = this.grid.getLocation(this).getX();
		this.y = this.grid.getLocation(this).getY();
		
		this.MAX_HEALTH = maxHealth;
		this.health = maxHealth;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 1)
	public void updateValues() {
		this.alphaSinucleinThreshold = this.policy.getParam(Policy.StatType.ALPHA_SINUCLEIN_THRESHOLD).getEffectiveValue();
		this.cytokineThreshold = this.policy.getParam(Policy.StatType.CYTO_NEURON_THRESHOLD).getEffectiveValue();
		this.degenerationRate = this.policy.getParam(Policy.StatType.DEGENERATION_RATE).getEffectiveValue();
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
            	if(alphaValue >= alphaSinucleinThreshold || cytokineValue >= cytokineThreshold) this.state = NeuronState.STRESSED;
            	Treatment x = (Treatment) this.context.getObjectsAsStream(Treatment.class).findFirst().get();
            	if(x.getToxicity() >= 1) this.state = NeuronState.STRESSED;
            break;
                
            case STRESSED:
            	if(alphaValue < alphaSinucleinThreshold && cytokineValue < cytokineThreshold && this.health > 25) this.state = NeuronState.HEALTHY;
            	
            	if(this.health > 0){
            		this.loseHealth();
            		this.regenHealth();
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
 	    //System.out.println("cytoValueInNeuron: " + cytokineValue);	 
	}


	private void absorbSynuclein() {
				
		//TODO
		int i = 3;
	
		double oldValue = this.alphaValueLayer.get(x, y);
		double absorbedValue = oldValue / i;
		double newValue = oldValue - absorbedValue;
	
 	    alphaValueLayer.set(newValue, (int) this.x, (int) this.y);
 	    this.alphaValue = this.alphaValue + absorbedValue;
 	    //System.out.println("alphaValueInNeuron: " + alphaValue);	 
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
	
	public void regenHealth() {
		Treatment x = (Treatment) this.context.getObjectsAsStream(Treatment.class).findFirst().get();
		if(health < 30) {
			if(x.getToxicity() < 1 && alphaValue < alphaSinucleinThreshold && cytokineValue < cytokineThreshold) 
				this.health = this.health + 0.5;
		}
	
	
	}
	
	public void loseHealth() {
		Treatment x = (Treatment) this.context.getObjectsAsStream(Treatment.class).findFirst().get();
		this.health = this.health - (this.policy.getParam(StatType.DEGENERATION_RATE).getEffectiveValue()) - x.getToxicity();
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
	
	public double getHealth() {
		return health;
	}
	
	public void setCytokineValue(double cytokineValue) {
		this.cytokineValue = cytokineValue;
	}
	
}
