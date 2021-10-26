package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class Replace extends StringFunction {

	public Replace(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 2) {
			String str = string.getString();
			String search = arguments[0].toString();
			String newvalue = arguments[1].toString();
			String ret = str.replace(search, newvalue);
			return new SString(ret);
		} else {
			throw new ScriptCallException("reaplce requires at least 2 argument");
		}
	}

}
