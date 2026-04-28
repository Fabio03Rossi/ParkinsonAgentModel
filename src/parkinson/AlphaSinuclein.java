package parkinson;

public class AlphaSinuclein {
	
private int value;
private int range;
	
	public AlphaSinuclein(int value, int range) {
		this.value = value;
		this.range = range;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getRange() {
		return range;
	}
	
	public void setRange(int range) {
		this.range = range;
	}

}
