package io.firebus.script.values.callables.impl.math;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Random extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		double r = Math.random();
		return new SNumber(r);
	}

}
