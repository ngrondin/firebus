package io.firebus.script.values.impl;

import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.json.Stringify;

public class JSONStaticPackage extends SPredefinedObject {

	public JSONStaticPackage() {
		
	}
	
	public String[] getMemberKeys() {
		return new String[] {"stringify", "parse"};
	}

	public SValue getMember(String name)  {
		if(name.equals("stringify")) {
			return new Stringify();
		} 
		return null;
	}

}
