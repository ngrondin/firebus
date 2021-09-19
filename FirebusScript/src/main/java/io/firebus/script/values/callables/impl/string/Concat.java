package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class Concat extends StringFunction {

	public Concat(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		String str = string.getString();
		for(int i = 0; i < arguments.length; i++)
			str += arguments[i].toString();
		return new SString(str);
	}

}
