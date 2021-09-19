package io.firebus.script.values.callables.impl;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class StaticGetter extends SCallable {
	protected SValue value;
	
	public StaticGetter(SValue v) {
		value = v;
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		return value;
	}

}
