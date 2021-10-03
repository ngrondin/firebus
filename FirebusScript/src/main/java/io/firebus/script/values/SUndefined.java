package io.firebus.script.values;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SValue;

public class SUndefined extends SValue {
	private static SUndefined singelton = new SUndefined();
	
    private SUndefined() {
    }

    public static SUndefined get() {
    	return singelton;
    }
    
 	public boolean equals(SValue other) {
		return other instanceof SUndefined || other instanceof SNull;
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

	public Number toNumber() throws ScriptValueException {
		throw new ScriptValueException("Undefined cannot be converted to number");
	}

	public Boolean toBoolean() throws ScriptValueException {
		throw new ScriptValueException("Undefined cannot be converted to boolean");
	}
}
