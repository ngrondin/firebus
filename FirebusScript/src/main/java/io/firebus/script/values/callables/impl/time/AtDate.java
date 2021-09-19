package io.firebus.script.values.callables.impl.time;

import java.time.ZonedDateTime;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.STime;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.TimeFunction;

public class AtDate extends TimeFunction {

	public AtDate(STime t) {
		super(t);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			if(v instanceof SDate) {
				SDate d = ((SDate)v);
				ZonedDateTime newZdt = time.getTime().atDate(d.getZonedDateTime());
				return new SDate(newZdt);
			} else {
				throw new ScriptCallException("atDate requires a Date argument");
			}
		} else {
			throw new ScriptCallException("atDate requires 1 argument;");
		}
	}

}
