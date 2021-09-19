package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Push extends ArrayFunction {
	
	public Push(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue val = arguments[0];
			values.add(val);
		} 
		return new SNull();
	}

}
