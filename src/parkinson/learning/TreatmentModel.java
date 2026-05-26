package parkinson.learning;

import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

public class TreatmentModel extends LearningModel {


   
	public TreatmentModel(List<Action> pa) {
		super(pa);
	}
	
   @Override
   public double getMaxFutureValue(State state) {
      double bestValue = Double.NEGATIVE_INFINITY; 
      
      // Per ogni azione possibile
      for (Action myAction : possibleActions) {
         double actionValue = this.getValue(new StateAction(state, myAction));
         if (actionValue > bestValue) {
               bestValue = actionValue;
         }

      }
      
      return bestValue == Double.NEGATIVE_INFINITY ? 0.0 : bestValue;
   }
   
}
