package io.firebus.script.values.callables.impl.date;

import java.time.format.DateTimeFormatter;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class ToDateString extends DateFunction {

	public ToDateString(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		String formatStr = (arguments.length >= 1 && arguments[0] instanceof SString) ? arguments[0].toString() : "E MMM d YYYY";
		return new SString(date.getZonedDateTime().toLocalDate().format(DateTimeFormatter.ofPattern(formatStr)));
	}

}
