package parkinson;

public class ImmuneCompromisedDisease extends HealthDisease {

	protected double diffusionRateModifier;
	protected double evaporationRateModifier;
	
	@Override
	public double getDiffusionRateMod() {
		return 1.0f * diffusionRateModifier;
	}
	
	@Override
	public double getEvaporationRateMod() {
		return 1.0f * evaporationRateModifier;
	}
	
}
