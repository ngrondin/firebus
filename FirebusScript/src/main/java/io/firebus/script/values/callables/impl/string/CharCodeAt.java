package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class CharCodeAt extends StringFunction {

	public CharCodeAt(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			if(v instanceof SNumber) {
				int i = ((SNumber)v).getNumber().intValue();
				if(i > 0 && i < string.getString().length()) {
					char c = string.getString().charAt(i);
					return new SNumber((int)c);
				} else {
					throw new ScriptException("index out of range"); 
				}
			} else {
				throw new ScriptException("charCodeAt requies number arguments");
			}
		} else {
			throw new ScriptException("charCodeAt requires at least 1 argument");
		}
	}

}
