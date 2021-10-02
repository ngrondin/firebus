package io.firebus.script.values.callables.impl;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class ToString extends SCallable {
	protected SValue value;
	
	public ToString(SValue v) {
		value = v;
	}
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		return new SString(value.toString());
	}

}
