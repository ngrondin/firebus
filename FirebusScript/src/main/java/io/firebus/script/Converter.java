package io.firebus.script;

import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;

public class Converter {

	protected SValue convertIn(Object o) {
		if(o == null) {
			return new SNull();
		} else if(o instanceof SValue) {
			return (SValue)o;
		} else if(o instanceof Number) {
			return new SNumber((Number)o);
		} else if(o instanceof String) {
			return new SString((String)o);
		} else if(o instanceof Boolean) {
			return new SBoolean((Boolean)o);
		} 		
		return null;
	}
	
	protected Object convertOut(SValue v) {
		if(v instanceof SNull) {
			return null;
		} else if(v instanceof SNumber) {
			Number n = ((SNumber)v).getNumber();
			if(n instanceof Integer)
				return n.intValue();
			else if(n instanceof Float)
				return n.floatValue();
			else if(n instanceof Double)
				return n.doubleValue();
			else
				return null;
		} else if(v instanceof SString) {
			return ((SString)v).getString();
		} else if(v instanceof SBoolean) {
			return ((SBoolean)v).getBoolean();
		} else {
			return null;
		}
	}
}
