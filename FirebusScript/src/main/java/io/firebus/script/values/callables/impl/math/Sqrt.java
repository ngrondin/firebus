package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Sqrt extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length == 1) {
			try {
				return new SNumber(Math.sqrt(arguments[0].toNumber().doubleValue()));
			} catch(ScriptValueException e) {
				throw new ScriptCallException("Error in round", e);
			}
		} else {
			throw new ScriptCallException("sqrt requires 1 argument");
		}
	}

}
