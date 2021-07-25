package io.firebus.script.values;

import java.util.Map;

public class SNumber extends PredefinedSObject {
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

    public String toString() {
        return number.toString();
    }

	public boolean equals(SValue other) {
		return other instanceof SNumber && number.doubleValue() == ((SNumber)other).getNumber().doubleValue();
	}

	public boolean identical(SValue other) {
		return this == other;
	}
    
}
