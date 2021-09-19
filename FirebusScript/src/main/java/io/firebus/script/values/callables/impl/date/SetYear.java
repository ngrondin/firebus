package io.firebus.script.values.callables.impl.date;

import java.time.ZonedDateTime;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class SetYear extends DateFunction {

	public SetYear(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			if(v instanceof SNumber) {
				int i = ((SNumber)v).getNumber().intValue();
				ZonedDateTime zdt = date.getZonedDateTime();
				ZonedDateTime nzdt = ZonedDateTime.of(i, zdt.getMonth().getValue(), zdt.getYear(), zdt.getHour(), zdt.getMinute(), zdt.getSecond(), zdt.getNano(), zdt.getZone());
				date.setZonedDateTime(nzdt);
				return new SNumber(nzdt.toInstant().toEpochMilli());
			} else {
				throw new ScriptException("setYear requires a number");
			}
		} else {
			throw new ScriptException("setYear requires at lease 1 argument");
		}
	}

}
