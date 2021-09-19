package io.firebus.script.values.impl;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.STime;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class TimeConstructor extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		return new STime(arguments);
	}
}
