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
    
}
