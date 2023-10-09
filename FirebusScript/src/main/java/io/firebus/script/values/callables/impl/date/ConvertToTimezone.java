package io.firebus.script.values.callables.impl.date;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class ConvertToTimezone extends DateFunction {

	public ConvertToTimezone(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length == 1 && arguments[0] instanceof SString) {
			return new SDate(date, arguments[0]);
		} else {
			throw new ScriptCallException("convertToTimezeon requires 1 string argument");
		}
	}

}
