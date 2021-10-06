package io.firebus.script;

import java.util.LinkedHashMap;
import java.util.Map;

import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SValue;

public class Scope {
	protected Scope parent;
	protected Map<Integer, SValue> values;
	protected Map<Integer, VariableId> ids;
	
	public Scope() {
		values = new LinkedHashMap<Integer, SValue>();
		ids = new LinkedHashMap<Integer, VariableId>();
	}
	
	public Scope(Scope p) {
		parent = p;
		values = new LinkedHashMap<Integer, SValue>();
		ids = new LinkedHashMap<Integer, VariableId>();
	}
	
	public SValue getValue(VariableId id) {
		SValue ret = values.get(id.hash);
		if(ret != null) {
			return ret;
		} else {
			if(parent != null) {
				ret = parent.getValue(id);
				if(ret != null) {
					return ret;
				} else {
					return SUndefined.get();
				}
			} else {
				return SUndefined.get();
			}
		}
	}
	
	public void setValue(VariableId id, SValue value) {
		if(!updateValueIfExists(id, value)) {
			values.put(id.hash, value);
			ids.put(id.hash, id);
		}
	}
	
	protected boolean updateValueIfExists(VariableId id, SValue value) {
		if(values.containsKey(id.hash)) {
			values.put(id.hash, value);
			return true;
		} else if(parent != null) {
			return parent.updateValueIfExists(id, value);
		} else {
			return false;
		}
	}	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\r\n");
		for(Integer hash : ids.keySet()) {
			VariableId id = ids.get(hash);
			sb.append(" ");
			sb.append(id.name);
			sb.append(":");
			sb.append(values.get(hash).toString().replaceAll("(?m)^", " "));
			sb.append(",\r\n");
		}
		sb.append("}");
		if(parent != null) {
			sb.append(parent.toString());
			sb.append("\r\n");
		}		
		return sb.toString();
	}
}
