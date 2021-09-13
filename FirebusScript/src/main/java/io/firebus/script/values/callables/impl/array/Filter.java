package io.firebus.script.values.callables.impl.array;

import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Filter extends ArrayFunction {
	
	public Filter(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue... arguments) throws ScriptException {
		SArray ret = new SArray();
		SCallable c = (SCallable)arguments[0];
		int index = 0;
		for(int i = 0; i < values.size(); i++) {
			SValue item = values.get(i);
			SValue retBool = c.call(new SValue[] {item});
			if(retBool instanceof SBoolean) {
				boolean rb = ((SBoolean)retBool).getBoolean();
				if(rb == true)
					ret.set(index++, item);
			}
		}
		return ret;
	}

}
