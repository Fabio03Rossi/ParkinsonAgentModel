package parkinson;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

public class StateInstanceCreator implements InstanceCreator<State> {
	@Override
	public SubstanciaNigraState createInstance(Type type) {
		return new SubstanciaNigraState(0,0,0,0f,0f);
	}
}
