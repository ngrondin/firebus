package io.firebus.script.values.callables.impl.date;

import java.time.ZonedDateTime;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class SetHours extends DateFunction {

	public SetHours(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			if(v instanceof SNumber) {
				int i = ((SNumber)v).getNumber().intValue();
				ZonedDateTime zdt = date.getZonedDateTime();
				ZonedDateTime nzdt = ZonedDateTime.of(zdt.getYear(), zdt.getMonth().getValue(), zdt.getDayOfMonth(), i, zdt.getMinute(), zdt.getSecond(), zdt.getNano(), zdt.getZone());
				date.setZonedDateTime(nzdt);
				return new SNumber(nzdt.toInstant().toEpochMilli());
			} else {
				throw new ScriptException("setHours requires a number");
			}
		} else {
			throw new ScriptException("setHours requires at lease 1 argument");
		}
	}

}
