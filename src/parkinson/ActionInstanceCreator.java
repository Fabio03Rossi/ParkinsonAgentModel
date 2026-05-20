package parkinson;

import java.lang.reflect.Type;

import org.apache.commons.lang3.RandomUtils;

import com.google.gson.InstanceCreator;

public class ActionInstanceCreator implements InstanceCreator<Action> {

	@Override
	public Action createInstance(Type type) {
		return new Dosage(0f);
	}
}
