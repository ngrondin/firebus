package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Slice extends ArrayFunction {
	
	public Slice(SArray a) {
		super(a);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SValue start = arguments[0];
		SValue len = arguments.length >= 2 ? arguments[1] : new SNumber(1);
		if(start instanceof SNumber && len instanceof SNumber) {
			SArray ret = new SArray();
			int s = ((SNumber)start).getNumber().intValue();
			int e = s + ((SNumber)len).getNumber().intValue();
			int index = 0;
			for(int i = s; i < e; i++) {
				ret.set(index++, array.get(i));
			}
			return ret;
		} else {
			throw new ScriptCallException("Arguments of slice must be numbers");
		}
	}

}
