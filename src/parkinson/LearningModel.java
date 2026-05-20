package parkinson;

import java.util.HashMap;
import java.util.List;

public abstract class LearningModel {
	
   protected HashMap<StateAction, Double> actionValues;
   protected List<Action> possibleActions = null;
   private final static double learningRate = 0.1;
   private final static double discountFactor = 0.9;

	public LearningModel() {
      this.actionValues = new HashMap<>();
   }
	
	public LearningModel(List<Action> pa) {
	   this();
	   this.possibleActions = pa;
	}

   public void updateValue(StateAction stateAction, State nextState, double reward) {
      double currentValue = actionValues.getOrDefault(stateAction, 0.0);
      double newValue = currentValue + learningRate * (reward + discountFactor * getMaxFutureValue(nextState) - currentValue);
      System.out.println(stateAction.getState().toString());
      System.out.println(stateAction.hashCode());
      actionValues.put(stateAction, newValue);
      printValues();
   }

   public abstract double getMaxFutureValue(State nextState);

   public double getValue(StateAction stateAction) {
      return actionValues.getOrDefault(stateAction, 0.0);
   }

   public void printValues() {
      for (StateAction sa : actionValues.keySet()) {
         System.out.println("State: " + sa.getState().toString() + ", Action: " + sa.getAgentAction().toString() + ", Value: " + actionValues.get(sa));
      }
   }
	
}
