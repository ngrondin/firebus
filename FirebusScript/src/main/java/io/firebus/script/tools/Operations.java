package io.firebus.script.tools;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;

public class Operations {

	public static SValue add(SValue v1, SValue v2) throws ScriptException {
		if(v1 instanceof SString || v2 instanceof SString) {
			return new SString(v1.toString() + v2.toString());
		} else if(v1 instanceof SNumber && v2 instanceof SNumber) {
			Number n1 = ((SNumber)v1).getNumber();
			Number n2 = ((SNumber)v2).getNumber();
			Number r = null;
			if(n1 instanceof Integer && n2 instanceof Integer) {
				r = n1.intValue() + n2.intValue();
			} else {
				r = n1.doubleValue() + n2.doubleValue();
			}
			return new SNumber(r);
		} else {
			throw new ScriptException("Invalid expressions for add operator");
		}
	}
	
	public static SValue substract(SValue v1, SValue v2) throws ScriptException {
		if(v1 instanceof SNumber && v2 instanceof SNumber) {
			Number n1 = ((SNumber)v1).getNumber();
			Number n2 = ((SNumber)v2).getNumber();
			Number r = null;
			if(n1 instanceof Integer && n2 instanceof Integer) {
				r = n1.intValue() - n2.intValue();
			} else {
				r = n1.doubleValue() - n2.doubleValue();
			}
			return new SNumber(r);
		} else {
			throw new ScriptException("Both expressions of a substraction need to be numbers");
		}
	}
	
	public static SValue multiply(SValue v1, SValue v2) throws ScriptException {
		if(v1 instanceof SNumber && v2 instanceof SNumber) {
			Number n1 = ((SNumber)v1).getNumber();
			Number n2 = ((SNumber)v2).getNumber();
			Number r = null;
			if(n1 instanceof Integer && n2 instanceof Integer) {
				r = n1.intValue() * n2.intValue();
			} else {
				r = n1.doubleValue() * n2.doubleValue();
			}
			return new SNumber(r);
		} else {
			throw new ScriptException("Both expressions of a multiplication need to be numbers");
		}
	}
	
	public static SValue divide(SValue v1, SValue v2) throws ScriptException {
		if(v1 instanceof SNumber && v2 instanceof SNumber) {
			Number n1 = ((SNumber)v1).getNumber();
			Number n2 = ((SNumber)v2).getNumber();
			Number r = null;
			if(n1 instanceof Integer && n2 instanceof Integer) {
				r = n1.intValue() / n2.intValue();
			} else {
				r = n1.doubleValue() / n2.doubleValue();
			}
			return new SNumber(r);
		} else {
			throw new ScriptException("Both expressions of a division need to be numbers");
		}
	}
	
}
