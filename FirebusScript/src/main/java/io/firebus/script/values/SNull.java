package io.firebus.script.values;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SValue;

public class SNull extends SValue {

    public SNull() {
    }


	public boolean equals(SValue other) {
		return other instanceof SNull;
	}

	public boolean identical(SValue other) {
		return other instanceof SNull;
	}
    
	public String typeOf() {
		return "null";
	}


    public String toString() {
        return "null";
    }
    
    public Number toNumber() throws ScriptException {
		return 0;
	}
	
	public boolean toBoolean() throws ScriptException {
		return false;
	}
}
