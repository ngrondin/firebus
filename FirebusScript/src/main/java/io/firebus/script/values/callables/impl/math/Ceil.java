package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Ceil extends SCallable {

	public SValue call(SValue... arguments) throws ScriptException {
		if(arguments.length == 1) {
			SValue v = arguments[0];
			if(v instanceof SNumber) {
				Number n = ((SNumber)v).getNumber();
				return new SNumber(n.intValue() + 1);
			} else {
				throw new ScriptException("ceil requires a numeric value");
			}
		} else {
			throw new ScriptException("ceil requires 1 argument");
		}
	}

}
