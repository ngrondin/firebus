package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class FromCharCode extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length == 1) {
			SValue v = arguments[0];
			try {
				int i = v.toNumber().intValue();
				return new SString(Character.toString(i));
			} catch(ScriptValueException e) {
				throw new ScriptCallException("Error in fromCharCode", e);
			}
		} else {
			throw new ScriptCallException("fromCharCode requires 1 argument");
		}
	}

}
