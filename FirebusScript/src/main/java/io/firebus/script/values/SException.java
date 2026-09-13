package io.firebus.script.values;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;

public class SException extends SPredefinedObject {
	protected static String[] members = {"message", "stack", "cause"};
	protected Exception exception;
	
	public SException(Exception e) {
		exception = e;
	}

	public String[] getMemberKeys() {
		return members;
	}

	public Exception getException() {
		return exception;
	}
	
	public SValue getMember(String name) {
		if(name.equals("message")) {
			return new SString(exception.getMessage());
		} else if(name.equals("stack")) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			return new SString(sw.toString());
		} else if(name.equals("cause") && exception.getCause() instanceof Exception) {
			return new SException((Exception)exception.getCause());
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
	
	public Boolean toBoolean() throws ScriptValueException {
		throw new ScriptValueException("Exception cannot be converted to boolean");
	}
}
