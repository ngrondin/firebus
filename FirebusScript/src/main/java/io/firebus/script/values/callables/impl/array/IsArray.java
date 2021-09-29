package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class IsArray extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 1) {
			if(arguments[0] instanceof SArray) {
				return SBoolean.get(true);
			} else {
				return SBoolean.get(false);
			}
		} else {
			throw new ScriptCallException("isArray requires at least 1 argument");
		}
	}

}
