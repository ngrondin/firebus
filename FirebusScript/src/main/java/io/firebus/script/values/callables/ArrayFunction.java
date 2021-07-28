package io.firebus.script.values.callables;

import java.util.List;

import io.firebus.script.values.SCallable;
import io.firebus.script.values.SValue;

public abstract class ArrayFunction extends SCallable {
	protected List<SValue> values;
	
	public ArrayFunction(List<SValue> v) {
		values = v;
	}
}
