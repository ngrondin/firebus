package io.firebus.script.units;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public abstract class ExecutionUnit {
	
	ExecutionUnit() {
		
	}
	
	public abstract SValue eval(Scope scope);
}
