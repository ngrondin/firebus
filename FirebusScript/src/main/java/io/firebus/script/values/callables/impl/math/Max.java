package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Max extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 2) {
			SValue max = null;
			double maxD = 0;
			for(int i = 0; i < arguments.length; i++) {
				SValue v = arguments[i];
				if(v instanceof SNumber) {
					double d = ((SNumber)v).getNumber().doubleValue();
					if(max == null || (max != null && d > maxD)) {
						max = v;
						maxD = d;
					} 
				} else {
					throw new ScriptCallException("max requires only numeric arguments");
				}
			}
			return max;
		} else {
			throw new ScriptCallException("max requires at least 2 arguments");
		}
	}

}
