package io.firebus.script.scopes;

import java.util.HashMap;
import java.util.Map;

import io.firebus.script.values.SValue;

public class Scope {
	protected Scope parent;
	protected Map<String, SValue> values;
	
	public Scope() {
		values = new HashMap<String, SValue>();
	}
	
	public Scope(Scope p) {
		parent = p;
		values = new HashMap<String, SValue>();
	}
	
	public SValue getValue(String key) {
		return values.containsKey(key) ? values.get(key) : parent != null ? parent.getValue(key) : null;
	}
	
	public void setValue(String key, SValue value) {
		values.put(key, value);
	}
}
