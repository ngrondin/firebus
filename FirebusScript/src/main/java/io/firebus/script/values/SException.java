package io.firebus.script.values;

import io.firebus.script.exceptions.ScriptValueException;
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
		return SUndefined.get();
	}

	public String typeOf() {
		return "exception";
	}
	
	public String toString() {
		return exception.getMessage();
	}
	
	public Number toNumber() throws ScriptValueException {
		throw new ScriptValueException("Exception cannot be converted to number");
	}
	
	public boolean toBoolean() throws ScriptValueException {
		throw new ScriptValueException("Exception cannot be converted to boolean");
	}
}
