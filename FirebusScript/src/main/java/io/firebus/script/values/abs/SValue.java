package io.firebus.script.values.abs;

import io.firebus.script.exceptions.ScriptException;

public abstract class SValue {

	public abstract boolean equals(SValue other);
	
	public abstract boolean identical(SValue other);
	
	public abstract String typeOf();
	
	public abstract String toString();
	
	public abstract Number toNumber() throws ScriptException;
	
	public abstract boolean toBoolean() throws ScriptException;
}
