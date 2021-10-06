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
			if(v instanceof SString) {
				String s = ((SString)v).getString();
				try {
					return new SNumber(Double.parseDouble(s));
				} catch(Exception e) {
					return new SNumber(Double.NaN);
				}
			} else if(v instanceof SNumber) {
				Number n = ((SNumber)v).getNumber();
				return new SNumber(n.doubleValue());
			} else {
				return new SNumber(Double.NaN);		
			}
		} else {
			throw new ScriptCallException("parseFloat requires at least 1 argument");
		}
	}

}
