package io.firebus.script.values.impl;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class ParseFloat extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue v = arguments[0];
			double d = 0D;
			if(v instanceof SString) {
				String s = ((SString)v).getString();
				d = Double.parseDouble(s);
			} else if(v instanceof SNumber) {
				Number n = ((SNumber)v).getNumber();
				d = n.doubleValue();
			} else {
				throw new ScriptCallException("Invalid argument for parseFloat. Should be string.");				
			}
			return new SNumber(d);
		} else {
			throw new ScriptCallException("parseFloat requires at least 1 argument");
		}
	}

}
