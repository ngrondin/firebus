package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class StartsWith extends StringFunction {

	public StartsWith(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue ss = arguments[0];
			if(string.getString().indexOf(ss.toString()) > -1)
				return SBoolean.get(true);
			else 
				return SBoolean.get(false);
		} else {
			throw new ScriptCallException("includes requires at least 1 argument");
		}
	}

}
