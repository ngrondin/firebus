package io.firebus.script.values;

import io.firebus.script.values.abs.SValue;

public class SNull extends SValue {

    public SNull() {
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
    
	public String typeOf() {
		return "null";
	}
}
