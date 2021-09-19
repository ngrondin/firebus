package io.firebus.script.values.impl;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class DateConstructor extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		return new SDate(arguments);
	}
}
