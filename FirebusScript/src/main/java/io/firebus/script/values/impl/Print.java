package io.firebus.script.values.impl;


import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Print extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		SValue obj = arguments.length > 0 ? arguments[0] : null;
		System.out.println(obj != null ? obj.toString() : "");
		return SNull.get();
	}

}
