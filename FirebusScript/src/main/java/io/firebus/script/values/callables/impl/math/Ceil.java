package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Ceil extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length == 1) {
			try {
				Number n = arguments[0].toNumber();
				return new SNumber(n.longValue() + 1);
			} catch(ScriptValueException e) {
				throw new ScriptCallException("Error converting argument to number", e);
			}
		} else {
			throw new ScriptCallException("ceil requires 1 argument");
		}
	}

}
