package parkinson.learning;

import java.util.HashMap;
import java.util.List;
import parkinson.agent.passive.Treatment;
import parkinson.utils.FileManager;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.essentials.RepastEssentials;

public abstract class LearningModel {
   
   public HashMap<StateAction, Double> actionValues;
   protected List<Action> possibleActions = null;
   private final static double learningRate = 0.1;
   private final static double discountFactor = 0.9;

	public LearningModel() {
		this.actionValues = FileManager.load(Treatment.PATH + Treatment.JSON_NAME);
		if(actionValues == null)
			this.actionValues = new HashMap<>();
   }
	
	public LearningModel(List<Action> pa) {
	   this();
	   this.possibleActions = pa;
	}

   public void updateValue(StateAction stateAction, State nextState, double reward) {
      double currentValue = actionValues.getOrDefault(stateAction, 0.0);
      double newValue = currentValue + learningRate * (reward + discountFactor * getMaxFutureValue(nextState) - currentValue);
      actionValues.put(stateAction, newValue);
      //printValues();
   }

   public abstract double getMaxFutureValue(State nextState);

   public double getValue(StateAction stateAction) {
      return actionValues.getOrDefault(stateAction, 0.0);
   }

   public void printValues() {
      for (StateAction sa : actionValues.keySet()) {
         System.out.println("State: " + sa.getState().toString() + ", Action: " + sa.getAgentAction().toString() + ", Value: " + actionValues.get(sa) + sa.toString());
      }
   }	
}

/*
  	public static final String PATH = "E:\\projects\\eclipse-workspace\\Parkinson\\tabOutput\\";
	public static final String JSON_NAME = "learnMap.json";
	public static final String CSV_NAME = "rlConvergence.csv";

  	public void dataWrite() {
		FileManager.save(PATH + JSON_NAME, this.rlModel.actionValues);
		System.out.println("Map has been saved.");

		FileManager.logEpisodeData(
			PATH + CSV_NAME,
			RunEnvironment.getInstance().getParameters().getInteger("randomSeed") + "," + 
    	    		this.cumulativeReward + "," + 
    	    		this.currentState.getDegeneratedNeuron() + "," + 
    	    		(this.cumulativeAvgNeuronHP / RepastEssentials.GetTickCount()) + "," + 
    	    		(this.cumulativeDosage / RepastEssentials.GetTickCount()) + "," + 
    	    		this.epsilonProb + "," +
    	    		RepastEssentials.GetTickCount() + "\n"
		);
	}
*/