package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Reduce extends ArrayFunction {
	
	public Reduce(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SCallable c = (SCallable)arguments[0];
		SValue initial = arguments.length >= 2 ? arguments[1] : new SNull();
		SValue value = initial;
		for(int i = 0; i < values.size(); i++) {
			SValue item = values.get(i);
			value = c.call(new SValue[] {value, item});
		}
		return value;
	}

}
