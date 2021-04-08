package io.firebus.script.scopes;

import java.util.HashMap;
import java.util.Map;

import io.firebus.script.objects.ScriptObject;

public class Scope {
	protected Scope parent;
	protected Map<String, ScriptObject> values;
	
	public Scope() {
		values = new HashMap<String, ScriptObject>();
	}
	
	public Scope(Scope p) {
		parent = p;
		values = new HashMap<String, ScriptObject>();
	}
	
	public ScriptObject getValue(String key) {
		return values.containsKey(key) ? values.get(key) : parent != null ? parent.getValue(key) : null;
	}
	
	public void setValue(String key, ScriptObject value) {
		values.put(key, value);
	}
}
