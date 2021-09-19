package io.firebus.script.values.callables.impl;

import io.firebus.data.ZonedTime;
import io.firebus.script.values.abs.SCallable;

public abstract class TimeFunction extends SCallable {
	protected ZonedTime time;
	
	public TimeFunction(ZonedTime zt) {
		time = zt;
	}
}
