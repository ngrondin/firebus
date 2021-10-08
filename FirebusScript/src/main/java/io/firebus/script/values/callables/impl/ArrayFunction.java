package io.firebus.script.values.callables.impl;

import io.firebus.script.values.SArray;
import io.firebus.script.values.abs.SCallable;

public abstract class ArrayFunction extends SCallable {
	protected SArray array;
	
	public ArrayFunction(SArray a) {
		array = a;
	}
}
