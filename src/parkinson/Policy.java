 package parkinson;

import java.util.EnumMap;
import java.util.Map;

/**
  * La Policy determina le caratteristiche mutabili della simulazione.
  * Tenendo conto della fisiologia del paziente e degli stimuli esterni per calcolare
  * i corretti moltiplicatori.
  */
public class Policy {
	// Treatment
	protected boolean NLRB3inibitor;	 // Utilizzata per indicare se nel sistema è presente una quantità 
										 //sufficiente di inibitore per bloccare lo stato infiammatorio delle cellule gliali
	
	// Valori default
	protected double cytoRelease = 1;
	
	private final Map<StatType, ModifiableParameter> params = new EnumMap<>(StatType.class);
	
	
	public Policy(int age, boolean gender, HealthDisease healthAlteration, double cytoActTre, double cytoRelease, double alphaTre, double cytoTre, double degenRate) {
		params.put(StatType.CYTO_ACTIVATION_THRESHOLD, new ModifiableParameter(cytoActTre));
		params.put(StatType.CYTO_RELEASE_RATE, new ModifiableParameter(cytoRelease));
		params.put(StatType.ALPHA_SINUCLEIN_THRESHOLD, new ModifiableParameter(alphaTre));
		params.put(StatType.CYTOKINE_THRESHOLD, new ModifiableParameter(cytoTre));
		params.put(StatType.DEGENERATION_RATE, new ModifiableParameter(degenRate));
	}

	
	public boolean isNLRB3inibitor() {
		return NLRB3inibitor;
	}
	
	public void setNLRB3inibitor(boolean nLRB3inibitor) {
		NLRB3inibitor = nLRB3inibitor;
	}
	
	public double getValue(StatType type) {
		return this.params.get(type).getEffectiveValue();
	}
	
	public void addParameterMod(StatType type, double rateMod) {
		this.params.get(type).addModifier(rateMod);
	}
	
	public void setParameterMod(StatType type, double rateMod) {
		this.params.get(type).setModifier(rateMod);
	}
	
	
	public enum StatType {
	    CYTO_ACTIVATION_THRESHOLD, // Numero di cytokine necessarie per attivare la microglia
	    CYTO_RELEASE_RATE, // Numero di cytokine rilasciate dalla microglia nel value layer
	    ALPHA_SINUCLEIN_THRESHOLD, // Tossicità delle alpha necessarie per stressare il neurone
	    CYTOKINE_THRESHOLD, // Numero di cytokine necessarie per stressare il neurone
	    DEGENERATION_RATE // Vita persa del neurone stressato ad ogni step
	}
	
	public class ModifiableParameter {
	    private final double originalValue;
	    private double modifier;

	    public ModifiableParameter(double originalValue) {
	        this.originalValue = originalValue;
	        this.modifier = 1.0;
	    }

	    public void setModifier(double modifier) {
	        this.modifier = modifier;
	    }

	    public void addModifier(double delta) {
	        this.modifier += delta;
	    }

	    public double getEffectiveValue() {
	        return originalValue * modifier; 
	    }

	    public double getOriginalValue() {
	        return originalValue;
	    }

	    public double getModifier() {
	        return modifier;
	    }
	}
}
