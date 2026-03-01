package io.firebus.script.values;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;

public class SBytes extends SPredefinedObject {
	protected byte[] bytes;
	protected static String[] keys = {"length"};
	
	public SBytes() {
		super();
		bytes = new byte[0];
	}
	
	public SBytes(byte[] b ) {
		super();
		bytes = b;
	}
	
	public byte[] get() {
		return bytes;
	}
	
	public int getSize() {
		return bytes.length;
	}
	
	public String[] getMemberKeys() {
		return keys;
	}

	public SValue getMember(String key) {
		if(key != null) {
			if(key.equals("length")) {
				return new SNumber(bytes.length);
			} 
		} 
		return SUndefined.get();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("b[");
		for(byte b: bytes) {
			sb.append(b);
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public Number toNumber() throws ScriptValueException {
		throw new ScriptValueException("Array cannot be converted to number");
	}
	
	public Boolean toBoolean() throws ScriptValueException {
		throw new ScriptValueException("Array cannot be converted to boolean");
	}
	
}
