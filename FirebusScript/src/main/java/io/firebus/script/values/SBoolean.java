package io.firebus.script.values;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;

public class SBoolean extends SPredefinedObject {
    protected boolean value;

    public SBoolean(boolean v) {
    	value = v;
    }
    
    protected Map<String, SValue> defineMembers() {
        return null;
    }

    public boolean getBoolean() {
        return value;
    }

    public String toString() {
        return value == true ? "true" : "false";
    }
    
	public Number toNumber() throws ScriptException {
		throw new ScriptException("Boolean cannot be converted to number");
	}
	
	public boolean toBoolean() throws ScriptException {
		return value;
	}

	public boolean equals(SValue other) {
		return other instanceof SBoolean && value == ((SBoolean)other).getBoolean();
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
		return null;
	}
	
	public String typeOf() {
		return "boolean";
	}
    
}
