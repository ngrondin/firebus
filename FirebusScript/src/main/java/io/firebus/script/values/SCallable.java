package io.firebus.script.values;


import io.firebus.script.exceptions.ScriptException;

public abstract class SCallable extends SValue {

	public abstract SValue call(SValue[] arguments) throws ScriptException;
	

	public boolean equals(SValue other) {
		return this == other;
	}

	public boolean identical(SValue other) {
		return this == other;
	}
	
	public String toString() {
		return "callable()";
	}
}
