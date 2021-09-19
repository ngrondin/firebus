package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class EndsWith extends StringFunction {

	public EndsWith(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue ss = arguments[0];
			if(string.getString().endsWith(ss.toString()))
				return new SBoolean(true);
			else 
				return new SBoolean(false);
		} else {
			throw new ScriptCallException("endsWith requires at least 1 argument");
		}
	}

}
