package io.firebus.script.values.abs;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SString;

public abstract class SObject extends SValue {

	public boolean hasMember(String key) {
		String[] keys = getMemberKeys();
		for(int i = 0; i < keys.length; i++)
			if(keys[i].equals(key))
				return true;
		return false;
	}
	
	public abstract String[] getMemberKeys();
	
	public abstract SValue getMember(String name);
	
	public boolean equals(SValue other) {
		return this == other;
	}

	public boolean identical(SValue other) {
		return this == other;
	}
	
	public String typeOf() {
		return "object";
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\r\n");
		String[] keys = getMemberKeys();
		for(int i = 0; i < keys.length; i++) {
			String key = keys[i];
			sb.append("\t");
			sb.append(key);
			sb.append(": ");
			SValue val = getMember(key);
			if(val instanceof SString) {
				sb.append("\"" + val + "\"");
			} else {
				sb.append(val);
			}
			sb.append("\r\n");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public Number toNumber() throws ScriptValueException {
		throw new ScriptValueException("Object cannot be converted to number");
	}
	
	public boolean toBoolean() throws ScriptValueException {
		throw new ScriptValueException("Object cannot be converted to boolean");
	}
	
}
