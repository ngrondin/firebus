package io.firebus.script.values;

import java.util.Map;

public class SBoolean extends PredefinedSObject {
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

	public boolean equals(SValue other) {
		return other instanceof SBoolean && value == ((SBoolean)other).getBoolean();
	}

	public boolean identical(SValue other) {
		return other instanceof SBoolean && value == ((SBoolean)other).getBoolean();
	}
    
}
