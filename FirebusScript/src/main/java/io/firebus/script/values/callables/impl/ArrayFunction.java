package io.firebus.script.values.callables.impl;

import java.util.List;

import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public abstract class ArrayFunction extends SCallable {
	protected List<SValue> values;
	
	public ArrayFunction(List<SValue> v) {
		values = v;
	}
}
