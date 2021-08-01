package io.firebus.script.values;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.callables.ArrayJoin;
import io.firebus.script.values.callables.ArrayMap;

public class SArray extends PredefinedSObject {
	protected List<SValue> values;
	protected static String[] keys = {"length"};
	
	public SArray() {
		super();
		values = new ArrayList<SValue>();
	}
	
	public SArray(List<SValue> l) {
		super();
		values = l;
	}
	
	public SValue get(int i) {
		return values.get(i);
	}
	
	public int getSize() {
		return values.size();
	}
	
	public void remove(int i) {
		
	}
	
	public void set(int i, SValue v) throws ScriptException {
		if(i == values.size()) {
			values.add(v);
		} else if(i < values.size()) {
			values.remove(i);
			values.add(i, v);
		} else {
			throw new ScriptException("Array out of bound", null);
		}
	}
	
	public String[] getMemberKeys() {
		return keys;
	}

	public SValue getMember(String key) {
		if(key != null) {
			if(key.equals("length")) {
				return new SNumber(values.size());
			} else if(key.equals("map")) {
				return new ArrayMap(values);
			} else if(key.equals("join")) {
				return new ArrayJoin(values);
			} 
		} 
		return null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(SValue val: values) {
			if(sb.length() > 1) 
				sb.append(", ");
			if(val instanceof SString) {
				sb.append("\"" + val + "\"");
			} else {
				sb.append(val);
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
