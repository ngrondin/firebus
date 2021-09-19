package io.firebus.script.values.flow;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SValue;

public abstract class SFlow extends SValue {

	public boolean equals(SValue other) {
		return false;
	}

	public boolean identical(SValue other) {
		return false;
	}

	public String typeOf() {
		return "statement";
	}
	
	public Number toNumber() throws ScriptException {
		throw new ScriptException("Flow statement cannot be converted to number");
	}

	public boolean toBoolean() throws ScriptException {
		throw new ScriptException("Flow statement cannot be converted to boolean");
	}
	
}
