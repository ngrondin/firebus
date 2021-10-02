package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class PadStart extends StringFunction {

	public PadStart(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 2) {
			try {
				int i = arguments[0].toNumber().intValue();
				String s = arguments[1].toString();
				String b = string.getString();
				while(b.length() < i)
					b = s + b;
				return new SString(b);
			} catch(ScriptValueException e) {
				throw new ScriptCallException("Error in padStart", e);
			}
		} else {
			throw new ScriptCallException("padStart requires 2 arguments");
		}
	}

}
