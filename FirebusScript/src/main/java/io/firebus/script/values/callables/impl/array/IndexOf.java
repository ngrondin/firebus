package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class IndexOf extends ArrayFunction {
	
	public IndexOf(SArray a) {
		super(a);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SValue v = (SValue)arguments[0];
		for(int i = 0; i < array.getSize(); i++) {
			SValue item = array.get(i);
			if(item.equals(v))
				return new SNumber(i);
		}
		return new SNumber(-1);
	}

}
