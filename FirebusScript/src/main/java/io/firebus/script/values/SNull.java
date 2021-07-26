package io.firebus.script.values;


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
    
}
