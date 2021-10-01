package io.firebus.script.values.impl;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class ParseInt extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			long l = 0;
			if(v instanceof SString) {
				String s = ((SString)v).getString();
				l = Long.parseLong(s);
			} if(v instanceof SNumber) {
				Number n = ((SNumber)v).getNumber();
				l = n.longValue();
			} else {
				throw new ScriptCallException("Invalid argument for parseInt. Should be string.");				
			}
			return new SNumber(l);
		} else {
			throw new ScriptCallException("parseInt requires at least 1 argument");
		}
	}

}
