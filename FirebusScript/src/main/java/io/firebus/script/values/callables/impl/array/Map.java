package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Map extends ArrayFunction {
	
	public Map(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SArray ret = new SArray();
		SCallable c = (SCallable)arguments[0];
		for(int i = 0; i < values.size(); i++)
			ret.set(i, c.call(new SValue[] {values.get(i)}));
		return ret;
	}

}
