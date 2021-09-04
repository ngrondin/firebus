package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Pop extends ArrayFunction {
	
	public Pop(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue[] arguments) throws ScriptException {
		if(this.values.size() > 0) {
			int lastIndex = values.size() - 1;
			SValue val = values.get(lastIndex);
			values.remove(lastIndex);
			return val;
		} else {
			return new SNull();
		}
	}

}
