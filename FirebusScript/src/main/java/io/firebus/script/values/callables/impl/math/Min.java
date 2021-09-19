package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Min extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 2) {
			SNumber min = null;
			double minD = 0;
			for(int i = 0; i < arguments.length; i++) {
				SValue v = arguments[i];
				if(v instanceof SNumber) {
					double d = ((SNumber)v).getNumber().doubleValue();
					if(min == null || (min != null && d < minD)) {
						min = (SNumber)v;
						minD = d;
					} 
				} else {
					throw new ScriptCallException("min requires only numeric arguments");
				}
			}
			return min;
		} else {
			throw new ScriptCallException("min requires at least 2 arguments");
		}
	}

}
