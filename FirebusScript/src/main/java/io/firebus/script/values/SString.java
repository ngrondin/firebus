package io.firebus.script.objects;

public class ScriptString extends ScriptObject {
	protected String str;
	
	public ScriptString(String s) {
		str = s;
	}
	
	public String toString() {
		return str;
	}
}
