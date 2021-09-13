package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class ForEach extends ArrayFunction {
	
	public ForEach(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptException {
		SCallable c = (SCallable)arguments[0];
		for(int i = 0; i < values.size(); i++) {
			SValue item = values.get(i);
			c.call(new SValue[] {item});
		}
		return new SNull();
	}

}
