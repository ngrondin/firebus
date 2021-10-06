package io.firebus.script.values.callables.impl.number;

import java.text.DecimalFormat;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.NumberFunction;

public class ToFixed extends NumberFunction {
	
	public ToFixed(SNumber n) {
		super(n);
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		try {
			int n = 0;
			String format = "0";
			if(arguments.length > 0) 
				n = arguments[0].toNumber().intValue();
			if(n > 0) {
				format += ".";
				for(int i = 0; i < n; i++)
					format += "0";
			}
			DecimalFormat decimalFormat = new DecimalFormat(format);
			String str = decimalFormat.format(number.toNumber().doubleValue());
			return new SString(str);
		} catch(ScriptValueException e) {
			throw new ScriptCallException("Error in toFixed", e);
		}
	}

}
