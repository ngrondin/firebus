package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptRuntimeException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Slice extends ArrayFunction {
	
	public Slice(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue[] arguments) throws ScriptException {
		SValue start = arguments[0];
		SValue len = arguments.length >= 2 ? arguments[1] : new SNumber(1);
		if(start instanceof SNumber && len instanceof SNumber) {
			SArray ret = new SArray();
			int s = ((SNumber)start).getNumber().intValue();
			int e = s + ((SNumber)len).getNumber().intValue();
			int index = 0;
			for(int i = s; i < e; i++) {
				ret.set(index++, values.get(i));
			}
			return ret;
		} else {
			throw new ScriptRuntimeException("Arguments of slice ust be numbers");
		}
	}

}
