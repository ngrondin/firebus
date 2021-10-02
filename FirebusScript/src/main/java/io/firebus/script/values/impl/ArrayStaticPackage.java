package io.firebus.script.values.impl;

import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.array.IsArray;

public class ArrayStaticPackage extends SPredefinedObject {

	public ArrayStaticPackage() {
		
	}
	
	public String[] getMemberKeys() {
		return new String[] {"isArray"};
	}

	public SValue getMember(String name)  {
		if(name.equals("isArray")) {
			return new IsArray();
		}
		return SUndefined.get();
	}

}
