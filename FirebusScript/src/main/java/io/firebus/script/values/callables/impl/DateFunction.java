package io.firebus.script.values.callables.impl;


import io.firebus.script.values.SDate;
import io.firebus.script.values.abs.SCallable;

public abstract  class DateFunction extends SCallable {
	protected SDate date;
	
	public DateFunction(SDate d) {
		date = d;
	}
}
