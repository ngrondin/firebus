package io.firebus.script.values;


import io.firebus.data.ZonedTime;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptRuntimeException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;

public class STime extends SPredefinedObject {
	protected ZonedTime time;
	
	public STime(ZonedTime t) {
		time = t;
	}

	public STime(SValue ...arguments) throws ScriptException {
		if(arguments.length == 0) {
			time = new ZonedTime();
		} else if(arguments.length == 1) {
			SValue arg = arguments[0];
			if(!(arg instanceof SNull || arg instanceof SUndefined)){
				time = ZonedTime.parse(arg.toString());
			} else {
				throw new ScriptRuntimeException("Invalid Time constructor parameter '" + arg + "'");
			}
		} else if(arguments.length == 4) {
			int hours = ((SNumber)arguments[0]).getNumber().intValue();
			int minutes = ((SNumber)arguments[1]).getNumber().intValue();
			int seconds = ((SNumber)arguments[2]).getNumber().intValue();
			String tz = ((SString)arguments[3]).getString();
			time = new ZonedTime(hours, minutes, seconds, 0, tz);			
		} else {
			throw new ScriptException("Unknow Time constructor");
		}
	}
	
	public String[] getMemberKeys() {
		return null;
	}

	public SValue getMember(String name) {

		return null;
	}

	public ZonedTime getTime() {
		return time;
	}
	
	public String toString() {
		return time.toString();
	}
}
