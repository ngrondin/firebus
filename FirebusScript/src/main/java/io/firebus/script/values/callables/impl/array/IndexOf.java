package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class IndexOf extends ArrayFunction {
	
	public IndexOf(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptException {
		SValue v = (SValue)arguments[0];
		for(int i = 0; i < values.size(); i++) {
			SValue item = values.get(i);
			if(item.equals(v))
				return new SNumber(i);
		}
		return new SNumber(-1);
	}

}
