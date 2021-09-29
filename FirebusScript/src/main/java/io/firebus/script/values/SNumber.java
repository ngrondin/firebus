package io.firebus.script.values;

import java.util.Map;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;

public class SNumber extends SPredefinedObject {
    protected Number number;

    public SNumber(Number n) {   	
   		number = n;
    }
    
    protected Map<String, SValue> defineMembers() {
        return null;
    }

    public Number getNumber() {
        return number;
    }

	public boolean equals(SValue other)  {
		try {
			return number == other.toNumber();
		} catch(ScriptValueException e) {
			return false;
		}
	}

	public boolean identical(SValue other) {
		try {
			return other instanceof SNumber && other.toNumber() == number;
		} catch(ScriptValueException e) {
			return false;
		}	
	}
    
	public boolean hasMember(String key) {
		return false;
	}

	public String[] getMemberKeys() {
		return null;
	}

	public SValue getMember(String name) {
		return null;
	}
	
	public String typeOf() {
		return "number";
	}
	
    public String toString() {
        return number.toString();
    }

	public Number toNumber() throws ScriptValueException {
		return getNumber();
	}
	
	public boolean toBoolean() throws ScriptValueException {
		return number.doubleValue() == 1 ? true : false;
	}

}
