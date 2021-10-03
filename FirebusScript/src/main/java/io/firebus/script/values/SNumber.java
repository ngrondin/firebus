package io.firebus.script.values;

import java.util.Map;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ToString;

public class SNumber extends SPredefinedObject {
    protected Number number;

    public SNumber(Number n) {
    	if(n instanceof Long) 
    		number = n;
    	else if(n instanceof Integer)
    		number = ((Integer)n).longValue();
    	else if(n instanceof Double)
    		number = n;
    	else if(n instanceof Float)
    		number = ((Float)n).doubleValue();
    }
    
    protected Map<String, SValue> defineMembers() {
        return null;
    }

    public Number getNumber() {
        return number;
    }

	public boolean equals(SValue other)  {
		try {
			Number on = other.toNumber();
			if(on != null) {
				return number == on;
			} else {
				return false;//number.longValue() == 0;
			} 
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
		if(name.equals("toString")) {
			return new ToString(this);
		}
		return SUndefined.get();
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
	
	public Boolean toBoolean() throws ScriptValueException {
		if(number.longValue() == 1L)
			return true;
		else if(number.longValue() == 0L)
			return false;
		else 
			return null;
	}

}
