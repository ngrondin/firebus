package io.firebus.script.values;

import io.firebus.script.values.abs.SValue;

public class SUndefined extends SValue {

    public SUndefined() {
    }

    public String toString() {
        return "undefined";
    }

	public boolean equals(SValue other) {
		return other instanceof SUndefined;
	}

	public boolean identical(SValue other) {
		return other instanceof SUndefined;
	}
    
	public String typeOf() {
		return "undefined";
	}
}
