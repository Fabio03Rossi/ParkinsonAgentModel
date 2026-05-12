package parkinson;

public record ImmuneCompromisedDisease(double diffusionRateModifier, 
		double evaporationRateModifier) implements HealthDisease {
	
	@Override
	public double getDiffusionRateMod() {
		return this.diffusionRateModifier;
	}
	
	@Override
	public double getEvaporationRateMod() {
		return this.evaporationRateModifier;
	}
	
}
