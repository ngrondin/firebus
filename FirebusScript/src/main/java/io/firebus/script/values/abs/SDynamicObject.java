package io.firebus.script.values.abs;

import java.util.HashMap;
import java.util.Map;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SUndefined;


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

	public SValue getMember(String key) {
		SValue m = members.get(key);
		return m != null ? m : SUndefined.get();
	}
			
	public void putMember(String key, SValue value) throws ScriptValueException {
		members.put(key, value);
	}
	
	public void removeMember(String key) {
		members.remove(key);
	}
}
