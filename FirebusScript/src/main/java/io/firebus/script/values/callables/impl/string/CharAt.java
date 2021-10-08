package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class CharAt extends StringFunction {

	public CharAt(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			try {
				int i = arguments[0].toNumber().intValue();
				if(i < string.getString().length()) {
					return new SString(string.getString().substring(i, i + 1));
				} else {
					return new SString("");
				}
				
			} catch(ScriptValueException e) {
				throw new ScriptCallException("charAt requies a number arguments");
			}
		} else {
			throw new ScriptCallException("charAt requires at least 1 argument");
		}
	}

}
