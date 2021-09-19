package io.firebus.script.values.callables.impl.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class SetTime extends DateFunction {

	public SetTime(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			if(v instanceof SNumber) {
				long i = ((SNumber)v).getNumber().longValue();
				ZonedDateTime nzdt = Instant.ofEpochMilli(i).atZone(ZoneId.systemDefault());
				date.setZonedDateTime(nzdt);
				return new SNumber(nzdt.toInstant().toEpochMilli());
			} else {
				throw new ScriptException("setTime requires a number");
			}
		} else {
			throw new ScriptException("setTime requires at lease 1 argument");
		}
	}

}
