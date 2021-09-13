package io.firebus.script.values.abs;

import java.util.HashMap;
import java.util.Map;

import io.firebus.script.exceptions.ScriptException;

public abstract class SDynamicObject extends SObject {
	protected Map<String, SValue> members;
	
	public SDynamicObject() {
		members = new HashMap<String, SValue>();
	}
	
	public boolean hasMember(String key) {
		return members.containsKey(key);
	}

	public String[] getMemberKeys() {
		return members.keySet().toArray(new String[0]);
	}

	public SValue getMember(String key) throws ScriptException {
		return members.get(key);
	}
			
	public void putMember(String key, SValue value) throws ScriptException {
		members.put(key, value);
	}
	
	public void removeMember(String key) {
		members.remove(key);
	}
}
