package io.firebus.script;

public class VariableId {
	final public String name;
	final public int hash;
	
	public VariableId(String n) {
		name = n;
		hash = n.hashCode();
	}
	
	public int hashCode() {
		return hash;
	}
	
	public boolean equals(Object o) {
		if(o instanceof VariableId && name.equals(((VariableId)o).name))
			return true;
		else
			return false;
	}

}
