package io.firebus.script.values.impl;

import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.object.Entries;
import io.firebus.script.values.callables.impl.object.Keys;

public class ObjectStaticPackage extends SPredefinedObject {

	public ObjectStaticPackage() {
		
	}
	
	public String[] getMemberKeys() {
		return new String[] {"keys"};
	}

	public SValue getMember(String name)  {
		if(name.equals("keys")) {
			return new Keys();
		} else if(name.equals("entries")) {
			return new Entries();
		}
		return SUndefined.get();
	}

}
