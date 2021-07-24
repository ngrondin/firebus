package io.firebus.script.values;

import java.util.Map;

public class SString extends PredefinedSObject {
	protected String str;
	
	public SString(String s) {
		str = s;
	}

	protected Map<String, SValue> defineMembers() {
		return null;
	}

	public String getString() {
		return str;
	}

	public String toString() {
		return str;
	}
}
