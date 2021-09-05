package io.firebus.script.values.impl;


import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Print extends SCallable {

	public SValue call(SValue[] arguments) {
		SValue obj = arguments.length > 0 ? arguments[0] : null;
		System.out.println(obj != null ? obj.toString() : "");
		return new SNull();
	}

}
