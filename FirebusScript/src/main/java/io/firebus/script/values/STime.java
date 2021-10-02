package io.firebus.script.values;


import io.firebus.data.ZonedTime;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.time.AtDate;

public class STime extends SPredefinedObject {
	protected ZonedTime time;
	
	public STime(ZonedTime t) {
		time = t;
	}

	public STime(SValue ...arguments) throws ScriptCallException {
		if(arguments.length == 0) {
			time = new ZonedTime();
		} else if(arguments.length == 1) {
			SValue arg = arguments[0];
			if(!(arg instanceof SNull || arg instanceof SUndefined)){
				time = ZonedTime.parse(arg.toString());
			} else {
				throw new ScriptCallException("Invalid Time constructor parameter '" + arg + "'");
			}
		} else if(arguments.length == 4) {
			int hours = ((SNumber)arguments[0]).getNumber().intValue();
			int minutes = ((SNumber)arguments[1]).getNumber().intValue();
			int seconds = ((SNumber)arguments[2]).getNumber().intValue();
			String tz = ((SString)arguments[3]).getString();
			time = new ZonedTime(hours, minutes, seconds, 0, tz);			
		} else {
			throw new ScriptCallException("Unknow Time constructor");
		}
	}
	
	public String[] getMemberKeys() {
		return null;
	}

	public SValue getMember(String name) {
		if(name.equals("atDate")) {
			return new AtDate(this);
		}
		return SUndefined.get();
	}

	public ZonedTime getTime() {
		return time;
	}
	
	public String toString() {
		return time.toString();
	}
}
