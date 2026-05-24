package parkinson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public abstract class LearningModel {
	
   public final Gson gson = new GsonBuilder()
		   .registerTypeAdapter(State.class, new TypeAdapter<SubstanciaNigraState>() {
			@Override
			public SubstanciaNigraState read(JsonReader reader) throws IOException {
				if (reader.peek() == JsonToken.NULL) {
					reader.nextNull();
					return null;
				}
				String string = reader.nextString();
				String[] parts = string.split(",");
				int deg = Integer.parseInt(parts[0]);
				int str = Integer.parseInt(parts[1]);
				int inf = (int) Double.parseDouble(parts[2]);
				int neu = (int) Double.parseDouble(parts[3]);
				double hea = Double.parseDouble(parts[4]);
				double dos = Double.parseDouble(parts[5]);
				return new SubstanciaNigraState(deg, str, inf, neu, hea, dos);
			}
			@Override
			public void write(JsonWriter writer, SubstanciaNigraState value) throws IOException {
				if (value == null) {
					writer.nullValue();
					return;
				}
				String xy = value.getDegeneratedNeuron() 
						+ "," + value.getStressedNeuron() 
						+ "," + value.getInflammatedMicroglia() 
						+ "," + value.getActualDegenNeuron() 
						+ "," + value.getAverageNeuronHealth() 
						+ "," + value.getCurrentGLP1dose();
				writer.value(xy);
			}
		   })
		   .registerTypeAdapter(Action.class, new TypeAdapter<Dosage>() {
				@Override
				public Dosage read(JsonReader reader) throws IOException {
					if (reader.peek() == JsonToken.NULL) {
						reader.nextNull();
						return null;
					}
					String string = reader.nextString();
					String[] parts = string.split(",");
					double dos = Double.parseDouble(parts[0]);
					return new Dosage(dos);
				}
				@Override
				public void write(JsonWriter writer, Dosage value) throws IOException {
					if (value == null) {
						writer.nullValue();
						return;
					}
					Double xy = value.getDosage();
					writer.value(xy);
				}
			   })
		   .enableComplexMapKeySerialization()
		   .create();
   
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
      System.out.println(nextState.toString());
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
   
	
	public void save(String path) {
       var f = new File(Treatment.PATH + path);
       try {
           f.createNewFile();
    	    FileWriter f2 = new FileWriter(f, false);
    	    Type typeObject = new TypeToken<HashMap<StateAction, Double>>(){}.getType();
    	    f2.write(gson.toJson(actionValues, typeObject));
    	    f2.close();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}           
   }

   private HashMap<StateAction, Double> load(String path) {
	   File f = new File(Treatment.PATH + path);
       if (!f.exists()) return null;
       
       String data = "";
       
		try (Scanner myReader = new Scanner(f)) {
			while (myReader.hasNextLine()) {
			   data += myReader.nextLine();
			   
			}
		  } catch (FileNotFoundException e) {
		    e.printStackTrace();
		  }
		Type typeObject = new TypeToken<HashMap<StateAction, Double>>(){}.getType();
		var result = gson.fromJson(data, typeObject);
		System.out.println("JSON: " + result);
	   return (HashMap<StateAction, Double>) result;
   }
	
}
