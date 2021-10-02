package io.firebus.script.values.impl;

import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.string.FromCharCode;

public class StringStaticPackage extends SPredefinedObject {

	public StringStaticPackage() {
		
	}
	
	public String[] getMemberKeys() {
		return new String[] {"fromCharCode"};
	}

	public SValue getMember(String name)  {
		if(name.equals("fromCharCode")) {
			return new FromCharCode();
		} 
		return SUndefined.get();
	}

}
