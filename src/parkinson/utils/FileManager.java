package parkinson.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import parkinson.learning.Action;
import parkinson.learning.Dosage;
import parkinson.learning.State;
import parkinson.learning.StateAction;
import parkinson.learning.SubstanciaNigraState;

public class FileManager {
	   public final static Gson gson = new GsonBuilder()
		   .registerTypeAdapter(State.class, new TypeAdapter<SubstanciaNigraState>() {
			@Override
			public SubstanciaNigraState read(JsonReader reader) throws IOException {
				reader.setStrictness(Strictness.LENIENT);
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
	
	public static void save(String path, HashMap<StateAction, Double> actionValues) {
       var f = new File(path);
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

	public static HashMap<StateAction, Double> load(String path) {
	   	File f = new File(path);
       	if (!f.exists()) return null;
       
       	String data = "";
       
		try (Scanner myReader = new Scanner(f)) {
			while (myReader.hasNextLine()) {
				data += myReader.nextLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return gson.fromJson(data, new TypeToken<HashMap<StateAction, Double>>(){}.getType());
   	}
   
	public static void logEpisodeData(String path, String data) {
		var f = new File(path);
		try {
			f.createNewFile();
    	    FileWriter f2 = new FileWriter(f, true);
    	    f2.write(data);
    	    f2.close();
		} catch (IOException e) {
			e.printStackTrace();
    	}
	}
}