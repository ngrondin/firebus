package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class IndexOf extends StringFunction {

	public IndexOf(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue ss = arguments[0];
			return new SNumber(string.getString().indexOf(ss.toString()));
		} else {
			throw new ScriptCallException("startsWith requires at least 1 argument");
		}
	}

}
