package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Includes extends ArrayFunction {
	
	public Includes(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SValue v = (SValue)arguments[0];
		for(int i = 0; i < values.size(); i++) {
			SValue item = values.get(i);
			if(item.equals(v))
				return SBoolean.get(true);
		}
		return SBoolean.get(false);
	}

}
