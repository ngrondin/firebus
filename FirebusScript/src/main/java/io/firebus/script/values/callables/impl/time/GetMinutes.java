package io.firebus.script.values.callables.impl.time;


import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.STime;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.TimeFunction;

public class GetMinutes extends TimeFunction {

	public GetMinutes(STime t) {
		super(t);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		return new SNumber(time.getTime().getMinutes());
	}

}
