package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Map extends ArrayFunction {
	
	public Map(SArray a) {
		super(a);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SArray ret = new SArray();
		SCallable c = (SCallable)arguments[0];
		for(int i = 0; i < array.getSize(); i++)
			ret.set(i, c.call(new SValue[] {array.get(i)}));
		return ret;
	}

}
