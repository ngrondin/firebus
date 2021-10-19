package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class ToUpperCase extends StringFunction {

	public ToUpperCase(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		return new SString(string.getString().toUpperCase());
	}

}
