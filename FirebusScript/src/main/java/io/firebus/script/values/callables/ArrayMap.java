package io.firebus.script.values.callables;

import java.util.List;

import io.firebus.script.ScriptException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SCallable;
import io.firebus.script.values.SValue;

public class ArrayMap extends ArrayFunction {
	
	public ArrayMap(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue[] arguments) throws ScriptException {
		SArray ret = new SArray();
		SCallable c = (SCallable)arguments[0];
		for(int i = 0; i < values.size(); i++)
			ret.set(i, c.call(new SValue[] {values.get(i)}));
		return ret;
	}

}
