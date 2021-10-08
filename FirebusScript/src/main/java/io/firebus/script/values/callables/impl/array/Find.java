package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Find extends ArrayFunction {
	
	public Find(SArray a) {
		super(a);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SCallable c = (SCallable)arguments[0];
		for(int i = 0; i < array.getSize(); i++) {
			SValue item = array.get(i);
			SValue retBool = c.call(new SValue[] {item});
			if(retBool instanceof SBoolean) {
				boolean rb = ((SBoolean)retBool).getBoolean();
				if(rb == true)
					return item;
			}
		}
		return SNull.get();
	}

}
