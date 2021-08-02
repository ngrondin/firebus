package io.firebus.script.values;

import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;

public class SException extends SPredefinedObject {
	protected static String[] members = {"message"};
	protected Exception exception;
	
	public SException(Exception e) {
		exception = e;
	}

	public String[] getMemberKeys() {
		return members;
	}

	public SValue getMember(String name) {
		if(name.equals("message")) {
			return new SString(exception.getMessage());
		}
		return null;
	}

}
