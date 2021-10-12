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
			if(n1 instanceof Long && n2 instanceof Long) {
				r = n1.longValue() + n2.longValue();
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
		if(n1 instanceof Long && n2 instanceof Long) {
			r = n1.longValue() - n2.longValue();
		} else {
			r = n1.doubleValue() - n2.doubleValue();
		}
		return new SNumber(r);
	}
	
	public static SValue multiply(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		Number r = null;
		if(n1 instanceof Long && n2 instanceof Long) {
			r = n1.longValue() * n2.longValue();
		} else {
			r = n1.doubleValue() * n2.doubleValue();
		}
		return new SNumber(r);
	}
	
	public static SValue divide(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		Number r = null;
		if(n2.doubleValue() == 0D) {
			r = Double.POSITIVE_INFINITY;
		} else {
			if(n1 instanceof Long && n2 instanceof Long) {
				r = n1.longValue() / n2.longValue();
			} else {
				r = n1.doubleValue() / n2.doubleValue();
			}
		}
		return new SNumber(r);
	}
	
	public static SValue modulus(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		Number r = null;
		if(n2.doubleValue() == 0D) {
			r = Double.NaN;
		} else {
			if(n1 instanceof Long && n2 instanceof Long) {
				r = n1.longValue() % n2.longValue();
			} else {
				r = n1.doubleValue() % n2.doubleValue();
			}
		}
		return new SNumber(r);
	}
	
	public static SValue pow(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		Number r = Math.pow(n1.doubleValue(),  n2.doubleValue());
		return new SNumber(r);
	}
	
	public static SValue bitOr(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		return new SNumber(n1.longValue() | n2.longValue());
	}
	
	public static SValue bitAnd(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		return new SNumber(n1.longValue() & n2.longValue());
	}
	
	public static SValue bitXor(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		return new SNumber(n1.longValue() ^ n2.longValue());
	}
	
	public static SValue bitNot(SValue v1) throws ScriptValueException {
		Number n1 = v1.toNumber();
		return new SNumber(~(n1.longValue()));
	}
	
	public static SValue bitShiftLeft(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		long r = n1.longValue() << n2.longValue();
		return new SNumber(r);
	}
	
	public static SValue bitShiftRight(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		long r = n1.longValue() >> n2.longValue();
		return new SNumber(r);
	}
	
	public static SValue bitShiftRightLogical(SValue v1, SValue v2) throws ScriptValueException {
		Number n1 = v1.toNumber();
		Number n2 = v2.toNumber();
		long r = n1.longValue() >>> n2.longValue();
		return new SNumber(r);
	}
	
}
