package io.firebus.script.values.callables.impl.date;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class ToDateString extends DateFunction {

	public ToDateString(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptException {
		return new SString(date.getZonedDateTime().toString());
	}

}
