package io.firebus.script.values.impl;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class IsNaN extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			try {
				Number n = arguments[0].toNumber();
				if(n instanceof Double && Double.isNaN((Double)n)) {
					return SBoolean.get(true);
				} else {
					return SBoolean.get(false);
				}
			} catch(ScriptValueException e) {
				return SBoolean.get(true);
			}		
		} else {
			throw new ScriptCallException("isNaN requires at least 1 argument");
		}
	}

}
