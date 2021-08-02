package io.firebus.script.values.flow;

import io.firebus.script.values.abs.SValue;

public class SReturn extends SFlow {
	protected SValue returnedValue;
	
	public SReturn(SValue val) {
		returnedValue = val;
	}
	
	public SValue getReturnedValue() {
		return returnedValue;
	}

}
