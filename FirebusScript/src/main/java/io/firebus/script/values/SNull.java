package io.firebus.script.values;

import java.util.Map;

public class SNull extends PredefinedSObject {

    public SNull() {
    }
    
    protected Map<String, SValue> defineMembers() {
        return null;
    }

    public String toString() {
        return "null";
    }

	public boolean equals(SValue other) {
		return other instanceof SNull;
	}

	public boolean identical(SValue other) {
		return other instanceof SNull;
	}
    
}
