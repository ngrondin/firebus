package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class CharAt extends StringFunction {

	public CharAt(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			if(v instanceof SNumber) {
				int i = ((SNumber)v).getNumber().intValue();
				if(i > 0 && i < string.getString().length()) {
					String s = string.getString().substring(i, 1);
					return new SString(s);
				} else {
					throw new ScriptCallException("index out of range"); 
				}
			} else {
				throw new ScriptCallException("charAt requies number arguments");
			}
		} else {
			throw new ScriptCallException("charAt requires at least 1 argument");
		}
	}

}
