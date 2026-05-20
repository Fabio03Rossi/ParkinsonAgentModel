package parkinson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class LearningModel {
	
   public final Gson gson = new GsonBuilder().create();
   public HashMap<StateAction, Double> actionValues;
   protected List<Action> possibleActions = null;
   private final static double learningRate = 0.1;
   private final static double discountFactor = 0.9;

	public LearningModel() {
		this.actionValues = this.load("learnMap.json");
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
   
	
	public void save(String path) {
       var f = new File(path);
       try {
           f.createNewFile();
    	    FileWriter f2 = new FileWriter(f, false);
    	    f2.write(gson.toJson(actionValues.toString()));
    	    f2.close();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}           
   }

   private HashMap<StateAction, Double> load(String path) {
       File f = new File(path);
       if (!f.exists()) return null;
       
       String data = "";
       
		try (Scanner myReader = new Scanner(f)) {
			while (myReader.hasNextLine()) {
			   data = myReader.nextLine();
			}
		  } catch (FileNotFoundException e) {
		    e.printStackTrace();
		  }
    		   
	   return gson.fromJson(data, HashMap.class);
   }
	
}
