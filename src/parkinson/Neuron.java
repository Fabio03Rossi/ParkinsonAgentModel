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
	private int cytokineValue;
	private int alphaSinucleinValue;
	private int health;
	
	private final double x;
	private final double y;
	
	private int debris;
	private int alphaSinucleinTreshold;
	private int cytokineTreshold;
	
	private boolean flag;

	private static final Random rnd = new Random();
	
	public Neuron(Context context, int cytokineTreshold, int debris, int alphaSinucleinTreshold, int health) {
		
		super(context);
		this.space = (ContinuousSpace<Object>) context.getProjection("space");
		this.grid = (Grid<Object>) context.getProjection("grid");
		this.cytoValueLayer = (GridValueLayer) context.getValueLayer("cytoLayer");
		this.alphaValueLayer = (GridValueLayer) context.getValueLayer("alphaLayer");

		this.alphaSinucleinTreshold = alphaSinucleinTreshold;
		this.cytokineTreshold = cytokineTreshold;
		this.state = NeuronState.HEALTHY;
		this.debris = debris;
		
		this.x = space.getLocation(this).getX();
		this.y = space.getLocation(this).getY();

		this.flag = false;
		
		this.MAX_HEALTH = health;
		this.health = health;
	}

	
	@ScheduledMethod(start = 1, interval = 1, priority = 5)
	public void cytokineAbsorption() {
		int x;
		int y;
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
            	if(!flag) {
            		var originalVal = this.alphaValueLayer.get(this.grid.getLocation(this).getX(), this.grid.getLocation(this).getY());
            		this.alphaValueLayer.set(originalVal + (0.1 * alphaSinucleinValue), this.grid.getLocation(this).getX(), this.grid.getLocation(this).getY());
            		flag = true;
            	}
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
	
	public int getMaxHealth() {
		return MAX_HEALTH;
	}
	
	public int getHealth() {
		return health;
	}
	
	public void setCytokineValue(int cytokineValue) {
		this.cytokineValue = cytokineValue;
	}
	
}
