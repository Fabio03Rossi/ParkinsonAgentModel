package parkinson;

public class Dosage implements Action {
	
	private double dosage;
	//private double dosageNLRP;
	public Dosage(double x) {
		dosage = x;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return String.valueOf(dosage);
	}
	
	@Override
	public int hashCode(){
      int result;
      result = 31 * Double.valueOf(dosage).hashCode();
      return result;
	}
	
	@Override
	public String toString(){
      return getLabel();
	}
	
	public double getDosage() {
		return dosage;
	}
	
   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      Dosage player = (Dosage) obj;
      return this.hashCode() == player.hashCode();
   }
	
	
}