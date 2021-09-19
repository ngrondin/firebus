package io.firebus.script.values.impl;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.math.Ceil;
import io.firebus.script.values.callables.impl.math.Floor;
import io.firebus.script.values.callables.impl.math.Max;
import io.firebus.script.values.callables.impl.math.Min;

public class Math extends SPredefinedObject {

	public Math() {
		
	}
	
	public String[] getMemberKeys() {
		return null;
	}

	public SValue getMember(String name) throws ScriptException {
		if(name.equals("min")) {
			return new Min();
		} else if(name.equals("max")) {
			return new Max();
		} else if(name.equals("floor")) {
			return new Floor();
		} else if(name.equals("ceil")) {
			return new Ceil();
		}
		return null;
	}

}
