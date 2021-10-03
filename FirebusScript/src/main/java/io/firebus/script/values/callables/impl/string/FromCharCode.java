package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class FromCharCode extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		try {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < arguments.length; i++) {
				SValue v = arguments[i];
				int c = v.toNumber().intValue();
				sb.append(Character.toString(c));
			}
			return new SString(sb.toString());
		} catch(ScriptValueException e) {
			throw new ScriptCallException("Error in fromCharCode", e);
		}
	}

}
