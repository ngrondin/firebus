package io.firebus.script.values.callables.impl.date;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class ToISOString extends DateFunction {

	public ToISOString(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		return new SString(date.getZonedDateTime().toInstant().toString());
	}

}
