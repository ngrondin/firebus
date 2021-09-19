package io.firebus.script.values.callables.impl;

import io.firebus.script.values.STime;
import io.firebus.script.values.abs.SCallable;

public abstract class TimeFunction extends SCallable {
	protected STime time;
	
	public TimeFunction(STime t) {
		time = t;
	}
}
