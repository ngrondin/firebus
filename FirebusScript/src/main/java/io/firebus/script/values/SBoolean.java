package io.firebus.script.values;

import java.util.Map;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ToString;

public class SBoolean extends SPredefinedObject {
    protected boolean value;
    private static SBoolean sTrue = new SBoolean(true);
    private static SBoolean sFalse = new SBoolean(false);

    private SBoolean(boolean v) {
    	value = v;
    }
    
    public static SBoolean get(boolean v) {
    	if(v == true) return sTrue;
    	else return sFalse;
    }
    
    protected Map<String, SValue> defineMembers() {
        return null;
    }

    public boolean getBoolean() {
        return value;
    }

	public boolean equals(SValue other) {
		if(other instanceof SNull) {
			return false;
		} else if(other instanceof SNumber) {
			Number on = ((SNumber)other).getNumber();
			if(value == true && on.longValue() == 1L)
				return true;
			else if(value == false && on.longValue() == 0L)
				return true;
			else
				return false;
		} else if(other instanceof SString) {
			String os = ((SString)other).getString();
			if(value == true && (os.equals("true") || os.equals("1")))
				return true;
			else if(value == false && (os.equals("false") || os.equals("0") || os.equals("")))
				return true;
			else
				return false;
		} else if(other instanceof SBoolean) {
			Boolean ob = ((SBoolean)other).getBoolean();
			if(value == ob)
				return true;
			else
				return false;
		} else {
			return false;				
		}
	}

	public boolean identical(SValue other) {
		return other instanceof SBoolean && value == ((SBoolean)other).getBoolean();
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
		return "boolean";
	}
	
    public String toString() {
        return value == true ? "true" : "false";
    }
    
	public Number toNumber() throws ScriptValueException {
		return value == true ? 1L : 0L;
	}
	
	public Boolean toBoolean() throws ScriptValueException {
		return value;
	}
}
