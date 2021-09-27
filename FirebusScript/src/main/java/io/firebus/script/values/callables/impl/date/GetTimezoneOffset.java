package io.firebus.script.values.callables.impl.date;

import java.time.ZonedDateTime;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class GetTimezoneOffset extends DateFunction {

	public GetTimezoneOffset(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		ZonedDateTime zdt = date.getZonedDateTime();
		return new SNumber(zdt.getZone().getRules().getOffset(zdt.toInstant()).getTotalSeconds() / -60);
	}

}
