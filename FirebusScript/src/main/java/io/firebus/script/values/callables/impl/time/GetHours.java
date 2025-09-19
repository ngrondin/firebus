package io.firebus.script.values.callables.impl.time;


import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.STime;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.TimeFunction;

public class GetHours extends TimeFunction {

	public GetHours(STime t) {
		super(t);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		return new SNumber(time.getTime().getHours());
	}

}
