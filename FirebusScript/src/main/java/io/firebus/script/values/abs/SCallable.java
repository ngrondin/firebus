package io.firebus.script.values.abs;


import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;

public abstract class SCallable extends SValue {

	public abstract SValue call(SValue... arguments) throws ScriptCallException;


	public boolean equals(SValue other) {
		return this == other;
	}

	public boolean identical(SValue other) {
		return this == other;
	}
	
	public String toString() {
		return "callable()";
	}
	
	public Number toNumber() throws ScriptValueException {
		throw new ScriptValueException("Callable cannot be converted to number");
	}
	
	public Boolean toBoolean() throws ScriptValueException {
		throw new ScriptValueException("Callable cannot be converted to boolean");
	}
	
	public String typeOf() {
		return "callable";
	}

}
