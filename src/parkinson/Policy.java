package parkinson;

public record Policy (int age, boolean gender, HealthDisease healthAlteration){
	
	
	public double getNeuronDeathRate() {
		return 1.0f;
	}
	
	public double getMicrogliaActivationThreshold() {
		return 1.0f;
	}
	
	public double getDiffusionRateMod() {
		return 1.0f;
	}
	
	public double getEvaporationRateMod() {
		return 1.0f;
	}

}
