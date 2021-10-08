package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Concat extends ArrayFunction {
	
	public Concat(SArray a) {
		super(a);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		SArray ret = new SArray();
		int index = 0;
		for(int j = 0; j < this.array.getSize(); j++)
			ret.set(index++, this.array.get(j));	
		for(int i = 0; i < arguments.length; i++) {
			if(arguments[i] instanceof SArray) {
				SArray array = (SArray)arguments[i];
				for(int j = 0; j < array.getSize(); j++)
					ret.set(index++, array.get(j));				
			}
		}
		return ret;
	}

}
