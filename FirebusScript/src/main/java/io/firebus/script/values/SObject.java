package io.firebus.script.values;

import java.util.List;

public abstract class SObject extends SValue {

	public abstract List<String> getMemberNames();
	
	public abstract SValue getMember(String name);
	
	public abstract void setMember(String name, SValue value);
}
