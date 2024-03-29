package io.firebus.script.values;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.date.ConvertToTimezone;
import io.firebus.script.values.callables.impl.date.GetDate;
import io.firebus.script.values.callables.impl.date.GetDay;
import io.firebus.script.values.callables.impl.date.GetFullYear;
import io.firebus.script.values.callables.impl.date.GetHours;
import io.firebus.script.values.callables.impl.date.GetMilliseconds;
import io.firebus.script.values.callables.impl.date.GetMinutes;
import io.firebus.script.values.callables.impl.date.GetMonth;
import io.firebus.script.values.callables.impl.date.GetSeconds;
import io.firebus.script.values.callables.impl.date.GetTime;
import io.firebus.script.values.callables.impl.date.GetTimezoneOffset;
import io.firebus.script.values.callables.impl.date.GetYear;
import io.firebus.script.values.callables.impl.date.SetDate;
import io.firebus.script.values.callables.impl.date.SetFullYear;
import io.firebus.script.values.callables.impl.date.SetHours;
import io.firebus.script.values.callables.impl.date.SetMilliseconds;
import io.firebus.script.values.callables.impl.date.SetMinutes;
import io.firebus.script.values.callables.impl.date.SetMonth;
import io.firebus.script.values.callables.impl.date.SetSeconds;
import io.firebus.script.values.callables.impl.date.SetYear;
import io.firebus.script.values.callables.impl.date.ToDateString;
import io.firebus.script.values.callables.impl.date.ToISOString;
import io.firebus.script.values.callables.impl.date.ToLocaleDateString;
import io.firebus.script.values.callables.impl.date.ToLocaleTimeString;
import io.firebus.script.values.callables.impl.date.ToString;

public class SDate extends SPredefinedObject {
	protected ZonedDateTime date;
	
	public SDate(Date d) {
		date = d.toInstant().atZone(ZoneId.systemDefault());
	}
	
	public SDate(ZonedDateTime d) {
		date = d.withZoneSameInstant(ZoneId.systemDefault());
	}

	public SDate(SValue ...arguments) throws ScriptCallException {
		if(arguments.length == 0) {
			date = ZonedDateTime.now();
		} else if(arguments.length == 1) {
			SValue arg = arguments[0];
			if(arg instanceof SNumber) {
				date = Instant.ofEpochMilli(((SNumber)arg).getNumber().longValue()).atZone(ZoneId.systemDefault());
			} else if(arg instanceof SDate) {
				date = ((SDate)arg).getZonedDateTime();
			} else if(!(arg instanceof SNull || arg instanceof SUndefined)){
				date = ZonedDateTime.parse(arg.toString());
			} else {
				throw new ScriptCallException("Invalid Date constructor parameter '" + arg + "'");
			}
		} else if(arguments.length == 2) {
			SValue firstArg = arguments[0];
			if(firstArg instanceof SNumber) {
				date = Instant.ofEpochMilli(((SNumber)firstArg).getNumber().longValue()).atZone(ZoneId.of(arguments[1].toString()));
			} else if(firstArg instanceof SDate) {
				date = ((SDate)firstArg).getZonedDateTime().toInstant().atZone(ZoneId.of(arguments[1].toString()));
			}
		} else if(arguments.length == 3) {
			int year = ((SNumber)arguments[0]).getNumber().intValue();
			int month = ((SNumber)arguments[1]).getNumber().intValue();
			int dayOfMonth = ((SNumber)arguments[2]).getNumber().intValue();
			date = ZonedDateTime.of(year, month, dayOfMonth, 0, 0, 0, 0, ZoneId.systemDefault());
		} else if(arguments.length == 6 || arguments.length == 7) {
			int year = ((SNumber)arguments[0]).getNumber().intValue();
			int month = ((SNumber)arguments[1]).getNumber().intValue();
			int dayOfMonth = ((SNumber)arguments[2]).getNumber().intValue();
			int hours = ((SNumber)arguments[3]).getNumber().intValue();
			int minutes = ((SNumber)arguments[4]).getNumber().intValue();
			int seconds = ((SNumber)arguments[5]).getNumber().intValue();
			ZoneId zoneId = arguments.length == 7 ? ZoneId.of(arguments[6].toString()) : ZoneId.systemDefault();
			date = ZonedDateTime.of(year, month, dayOfMonth, hours, minutes, seconds, 0, zoneId);			
		} else {
			throw new ScriptCallException("Unknow Date constructor");
		}
	}
	
	public String[] getMemberKeys() {
		return null;
	}

	public SValue getMember(String name) {
		if(name.equals("toString")) {
			return new ToString(this);
		} else if(name.equals("toISOString")) {
			return new ToISOString(this);
		} else if(name.equals("toDateString")) {
			return new ToDateString(this);
		} else if(name.equals("toLocaleDateString")) {
			return new ToLocaleDateString(this);
		} else if(name.equals("toLocaleTimeString")) {
			return new ToLocaleTimeString(this);
		} else if(name.equals("getTimezoneOffset")) {
			return new GetTimezoneOffset(this);
		} else if(name.equals("getTime")) {
			return new GetTime(this);
		} else if(name.equals("getDay")) {
			return new GetDay(this);
		} else if(name.equals("getDate")) {
			return new GetDate(this);			
		} else if(name.equals("getFullYear")) {
			return new GetFullYear(this);
		} else if(name.equals("getHours")) {
			return new GetHours(this);
		} else if(name.equals("getMilliseconds")) {
			return new GetMilliseconds(this);
		} else if(name.equals("getMinutes")) {
			return new GetMinutes(this);
		} else if(name.equals("getMonth")) {
			return new GetMonth(this);
		} else if(name.equals("getSeconds")) {
			return new GetSeconds(this);
		} else if(name.equals("getYear")) {
			return new GetYear(this);
		} else if(name.equals("setFullYear")) {
			return new SetFullYear(this);
		} else if(name.equals("setDate")) {
			return new SetDate(this);			
		} else if(name.equals("setHours")) {
			return new SetHours(this);
		} else if(name.equals("setMilliseconds")) {
			return new SetMilliseconds(this);
		} else if(name.equals("setMinutes")) {
			return new SetMinutes(this);
		} else if(name.equals("setMonth")) {
			return new SetMonth(this);
		} else if(name.equals("setSeconds")) {
			return new SetSeconds(this);
		} else if(name.equals("setYear")) {
			return new SetYear(this);
		} else if(name.equals("convertToTimezone")) {
			return new ConvertToTimezone(this);
		} 
		return SUndefined.get();
	}

	public Date getDate() {
		return Date.from(date.toInstant());
	}
	
	public void setDate(Date dt) {
		date = ZonedDateTime.ofInstant(dt.toInstant(), date.getZone());
	}
	
	public ZonedDateTime getZonedDateTime() {
		return date;
	}
	
	public void setZonedDateTime(ZonedDateTime zdt) {
		date = zdt;
	}
	
	public String toString() {
		return Date.from(date.toInstant()).toString();
	}
	
	public Number toNumber() throws ScriptValueException {
		return date.toInstant().getEpochSecond();
	}
	
	public Boolean toBoolean() throws ScriptValueException {
		return true;
	}
	
	public String typeOf() {
		return "date";
	}
}
