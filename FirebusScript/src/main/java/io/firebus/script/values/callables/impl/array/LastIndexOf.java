package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class LastIndexOf extends ArrayFunction {
	
	public LastIndexOf(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SValue v = (SValue)arguments[0];
		for(int i = values.size() - 1; i >= 0; i--) {
			SValue item = values.get(i);
			if(item.equals(v))
				return new SNumber(i);
		}
		return new SNumber(-1);
	}

}
