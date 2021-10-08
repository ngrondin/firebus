package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Filter extends ArrayFunction {
	
	public Filter(SArray a) {
		super(a);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SArray ret = new SArray();
		SCallable c = (SCallable)arguments[0];
		int index = 0;
		for(int i = 0; i < array.getSize(); i++) {
			SValue item = array.get(i);
			SValue retBool = c.call(new SValue[] {item, new SNumber(i), array});
			if(retBool instanceof SBoolean) {
				boolean rb = ((SBoolean)retBool).getBoolean();
				if(rb == true)
					ret.set(index++, item);
			}
		}
		return ret;
	}

}
