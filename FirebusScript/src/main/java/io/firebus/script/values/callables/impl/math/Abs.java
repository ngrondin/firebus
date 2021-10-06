package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Abs extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length == 1) {
			try {
				Number n = arguments[0].toNumber();
				if(n instanceof Double) 
					return new SNumber(Math.abs((Double)n));
				else
					return new SNumber(Math.abs((Long)n));
			} catch(ScriptValueException e) {
				throw new ScriptCallException("Error in abs", e);
			}
		} else {
			throw new ScriptCallException("abs requires 1 argument");
		}
	}

}
