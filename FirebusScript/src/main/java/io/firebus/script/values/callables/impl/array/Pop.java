package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Pop extends ArrayFunction {
	
	public Pop(SArray a) {
		super(a);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		if(array.getSize() > 0) {
			int lastIndex = array.getSize() - 1;
			SValue val = array.get(lastIndex);
			array.remove(lastIndex);
			return val;
		} else {
			return SNull.get();
		}
	}

}
