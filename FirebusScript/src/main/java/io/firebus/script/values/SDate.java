package io.firebus.script.values;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptRuntimeException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StaticGetter;

public class SDate extends SPredefinedObject {
	protected ZonedDateTime date;
	
	public SDate(Date d) {
		date = d.toInstant().atZone(ZoneId.systemDefault());
	}

	public SDate(SValue ...arguments) throws ScriptException {
		if(arguments.length == 0) {
			date = ZonedDateTime.now();
		} else if(arguments.length == 1) {
			SValue arg = arguments[0];
			if(arg instanceof SNumber) {
				date = Instant.ofEpochMilli(((SNumber)arg).getNumber().longValue()).atZone(ZoneId.systemDefault());
			} else if(!(arg instanceof SNull || arg instanceof SUndefined)){
				date = ZonedDateTime.parse(arg.toString());
			} else {
				throw new ScriptRuntimeException("Invalid Date constructor parameter '" + arg + "'");
			}
		} else if(arguments.length == 3) {
			int year = ((SNumber)arguments[0]).getNumber().intValue();
			int month = ((SNumber)arguments[1]).getNumber().intValue();
			int dayOfMonth = ((SNumber)arguments[2]).getNumber().intValue();
			date = ZonedDateTime.of(year, month, dayOfMonth, 0, 0, 0, 0, ZoneId.systemDefault());
		} else if(arguments.length == 6) {
			int year = ((SNumber)arguments[0]).getNumber().intValue();
			int month = ((SNumber)arguments[1]).getNumber().intValue();
			int dayOfMonth = ((SNumber)arguments[2]).getNumber().intValue();
			int hours = ((SNumber)arguments[3]).getNumber().intValue();
			int minutes = ((SNumber)arguments[4]).getNumber().intValue();
			int seconds = ((SNumber)arguments[5]).getNumber().intValue();
			date = ZonedDateTime.of(year, month, dayOfMonth, hours, minutes, seconds, 0, ZoneId.systemDefault());			
		} else {
			throw new ScriptException("Unknow Date constructor");
		}
	}
	
	public String[] getMemberKeys() {
		return null;
	}

	public SValue getMember(String name) {
		if(name.equals("toString")) {
			return new StaticGetter(new SString(date.toString()));
		} else if(name.equals("toISOString")) {
			return new StaticGetter(new SString(date.toOffsetDateTime().toString()));
		} else if(name.equals("getTime")) {
			return new StaticGetter(new SNumber(date.toInstant().toEpochMilli()));
		}
		return null;
	}

	public Date getDate() {
		return Date.from(date.toInstant());
	}
	
	public String toString() {
		return date.toString();
	}
}
