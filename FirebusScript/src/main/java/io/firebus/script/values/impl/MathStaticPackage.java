package io.firebus.script.values.impl;

import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.math.Ceil;
import io.firebus.script.values.callables.impl.math.Floor;
import io.firebus.script.values.callables.impl.math.Max;
import io.firebus.script.values.callables.impl.math.Min;

public class MathStaticPackage extends SPredefinedObject {

	public MathStaticPackage() {
		
	}
	
	public String[] getMemberKeys() {
		return new String[] {"min", "max", "floor", "ceil"};
	}

	public SValue getMember(String name)  {
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
