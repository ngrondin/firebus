package io.firebus.script.values;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SValue;

public class SUndefined extends SValue {

    public SUndefined() {
    }

 	public boolean equals(SValue other) {
		return other instanceof SUndefined;
	}

	public boolean identical(SValue other) {
		return other instanceof SUndefined;
	}
    
	public String typeOf() {
		return "undefined";
	}

   public String toString() {
        return "undefined";
    }

	public Number toNumber() throws ScriptException {
		throw new ScriptException("Undefined cannot be converted to number");
	}

	public boolean toBoolean() throws ScriptException {
		throw new ScriptException("Undefined cannot be converted to boolean");
	}
}
