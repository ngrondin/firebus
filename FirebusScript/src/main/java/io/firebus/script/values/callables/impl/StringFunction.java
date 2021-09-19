package io.firebus.script.values.callables.impl;

import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;

public abstract class StringFunction extends SCallable {
	protected SString string;
	
	public StringFunction(SString s) {
		string = s;
	}

}
