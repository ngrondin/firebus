package io.firebus.script.values.callables.impl.date;

import java.time.ZonedDateTime;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class SetSeconds extends DateFunction {

	public SetSeconds(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			if(v instanceof SNumber) {
				int i = ((SNumber)v).getNumber().intValue();
				ZonedDateTime zdt = date.getZonedDateTime();
				ZonedDateTime nzdt = ZonedDateTime.of(zdt.getYear(), zdt.getMonth().getValue(), zdt.getDayOfMonth(), zdt.getHour(), zdt.getMinute(), i, zdt.getNano(), zdt.getZone());
				date.setZonedDateTime(nzdt);
				return new SNumber(nzdt.toInstant().toEpochMilli());
			} else {
				throw new ScriptCallException("setSeconds requires a number");
			}
		} else {
			throw new ScriptCallException("setSeconds requires at lease 1 argument");
		}
	}

}
