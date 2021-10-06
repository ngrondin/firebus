package io.firebus.script.values.callables.impl;

import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;

public abstract class NumberFunction extends SCallable {
	protected SNumber number;
	
	public NumberFunction(SNumber n) {
		number = n;
	}

}
