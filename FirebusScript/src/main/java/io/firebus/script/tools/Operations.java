package io.firebus.script.tools;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;

public class Operations {

	public static SValue add(SValue v1, SValue v2) throws ScriptValueException {
		if(v1 instanceof SString || v2 instanceof SString) {
			return new SString(v1.toString() + v2.toString());
		} else {
			Number n1 = v1.toNumber();
			Number n2 = v2.toNumber();
			Number r = null;
			if(n1 instanceof Integer && n2 instanceof Integer) {
				r = n1.intValue() + n2.intValue();
			} else {
				r = n1.doubleValue() + n2.doubleValue();
			}
			return new SNumber(r);
		}
	}
	
	public static SValue substract(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		Number r = null;
		if(n1 instanceof Integer && n2 instanceof Integer) {
			r = n1.intValue() - n2.intValue();
		} else {
			r = n1.doubleValue() - n2.doubleValue();
		}
		return new SNumber(r);
	}
	
	public static SValue multiply(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		Number r = null;
		if(n1 instanceof Integer && n2 instanceof Integer) {
			r = n1.intValue() * n2.intValue();
		} else {
			r = n1.doubleValue() * n2.doubleValue();
		}
		return new SNumber(r);
	}
	
	public static SValue divide(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		Number r = null;
		if(n1 instanceof Integer && n2 instanceof Integer) {
			r = n1.intValue() / n2.intValue();
		} else {
			r = n1.doubleValue() / n2.doubleValue();
		}
		return new SNumber(r);
	}
	
	public static SValue modulus(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		Number r = null;
		if(n1 instanceof Integer && n2 instanceof Integer) {
			r = n1.intValue() % n2.intValue();
		} else {
			r = n1.doubleValue() % n2.doubleValue();
		}
		return new SNumber(r);
	}
	
	public static SValue bitOr(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		return new SNumber(n1.intValue() | n2.intValue());
	}
	
	public static SValue bitAnd(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		return new SNumber(n1.intValue() & n2.intValue());
	}
	
	public static SValue bitXor(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		return new SNumber(n1.intValue() ^ n2.intValue());
	}
	
	public static SValue bitNot(SValue v1) throws ScriptValueException {
		Number n1 = v1.toNumber();
		return new SNumber(~(n1.intValue()));
	}
	
}
