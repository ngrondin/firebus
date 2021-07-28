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

	public boolean equals(SValue other) {
		return other instanceof SString && str.equals(((SString)other).getString());
	}

	public boolean identical(SValue other) {
		return this == other;
	}
	
	public boolean hasMember(String key) {
		return false;
	}

	public String[] getMemberKeys() {
		return null;
	}

	public SValue getMember(String name) {
		return null;
	}

}
