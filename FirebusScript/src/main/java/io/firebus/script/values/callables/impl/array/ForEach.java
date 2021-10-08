package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class ForEach extends ArrayFunction {
	
	public ForEach(SArray a) {
		super(a);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SCallable c = (SCallable)arguments[0];
		for(int i = 0; i < array.getSize(); i++) {
			SValue item = array.get(i);
			c.call(new SValue[] {item});
		}
		return SNull.get();
	}

}
