package io.firebus.script.values;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SValue;

public class SNull extends SValue {

	private static SNull singleton = new SNull();
	
	private SNull() {
		
	}
	
	public static SNull get() {
		return singleton;
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
    
    public Number toNumber() throws ScriptValueException {
		return 0L;
	}
	
	public Boolean toBoolean() throws ScriptValueException {
		return false;
	}
}
