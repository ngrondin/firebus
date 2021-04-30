package io.firebus.script.values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DynamicSObject extends SObject {
	protected List<String> memberNames;
	protected Map<String, SValue> members;
	
	public DynamicSObject() {
		memberNames = new ArrayList<String>();
		members = new HashMap<String, SValue>();
	}
	
	public List<String> getMemberNames() {
		return memberNames;
	}

	public SValue getMember(String name) {
		return members.get(name);
	}

	public void setMember(String name, SValue value) {
		if(!memberNames.contains(name))
			memberNames.add(name);
		members.put(name, value);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\r\n");
		for(String name : memberNames) {
			sb.append("\t");
			sb.append(name);
			sb.append(": ");
			sb.append(members.get(name));
			sb.append("\r\n");
		}
		sb.append("}");
		return sb.toString();
	}
}
