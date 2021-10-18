package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Atan2 extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length == 2) {
			try {
				return new SNumber(Math.atan2(arguments[0].toNumber().doubleValue(), arguments[1].toNumber().doubleValue()));
			} catch(ScriptValueException e) {
				throw new ScriptCallException("Error in atan2", e);
			}
		} else {
			throw new ScriptCallException("atan2 requires 2 arguments");
		}
	}

}
