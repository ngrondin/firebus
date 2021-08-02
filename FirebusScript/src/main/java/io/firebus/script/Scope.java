package io.firebus.script;

import java.util.HashMap;
import java.util.Map;

import io.firebus.script.values.abs.SValue;

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
	
	public boolean contains(String key) {
		return values.containsKey(key);
	}
	
	public SValue getValue(String key) {
		return values.containsKey(key) ? values.get(key) : parent != null ? parent.getValue(key) : null;
	}
	
	public void setValue(String key, SValue value) {
		values.put(key, value);
	}

	public Scope getScopeOf(String key) {
		return values.containsKey(key) ? this : parent != null ? parent.getScopeOf(key) : null;
	}
	
	/*public String toString() {
		return toString("");
	}*/
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		//sb.append(indent);
		sb.append("{");
		sb.append("\r\n");
		for(String key : values.keySet()) {
			//sb.append(indent);
			sb.append(" ");
			sb.append(key);
			sb.append(":");
			sb.append(values.get(key).toString().replaceAll("(?m)^", " "));
			sb.append(",\r\n");
		}
		if(parent != null) {
			sb.append(parent.toString().replaceAll("(?m)^", " "));
			sb.append("\r\n");
		}
		//sb.append(indent);
		sb.append("}");
		return sb.toString();
	}
}
