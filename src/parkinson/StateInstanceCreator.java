package parkinson;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

public class StateInstanceCreator implements InstanceCreator<State> {

	@Override
	public State createInstance(Type type) {
		// TODO Auto-generated method stub
		return new SubstanciaNigraState(0,0,0,0f,0f);
	}
}
