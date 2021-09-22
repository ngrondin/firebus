package io.firebus.script;

public class VariableId {
	final public String name;
	final public int hash;
	
	public VariableId(String n) {
		name = n;
		hash = n.hashCode();
	}

}
