package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Of extends ArrayFunction {
	
	public Of(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptException {
		SArray ret = new SArray();
		int index = 0;
		for(int i = 0; i < arguments.length; i++) {
			ret.set(index++, arguments[i]);		
		}
		return ret;
	}

}
