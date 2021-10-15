package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class LocaleCompare extends StringFunction {

	public LocaleCompare(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			String other = arguments[0].toString();
			int r = -1 * string.getString().compareTo(other);
			return new SNumber(r);
		} else {
			throw new ScriptCallException("localeCompare requires at least 1 argument");
		}
	}

}
