package io.firebus.script.values;

import java.util.Arrays;
import java.util.Map;

public abstract class PredefinedSObject extends DynamicSObject {

	public PredefinedSObject() {
		super();
		Map<String, SValue> predefinedMembers = defineMembers();
		if(predefinedMembers != null) {
			members = predefinedMembers;
			memberNames = Arrays.asList(members.keySet().toArray(new String[0]));
		}
	}
	
	protected abstract Map<String, SValue> defineMembers();
}
