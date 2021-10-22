package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Pow extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 2) {
			try {
				double base = arguments[0].toNumber().doubleValue();
				double exp = arguments[1].toNumber().doubleValue();
				return new SNumber(Math.pow(base, exp));
			
			} catch(ScriptValueException e) {
				throw new ScriptCallException("pow requires only numeric arguments");
			}
		} else {
			throw new ScriptCallException("pow requires at least 2 arguments");
		}
	}

}
