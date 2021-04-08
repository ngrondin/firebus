package io.firebus.script.units;

import io.firebus.script.objects.ScriptObject;
import io.firebus.script.scopes.Scope;

public abstract class ExecutionUnit {
	
	ExecutionUnit() {
		
	}
	
	public abstract ScriptObject eval(Scope scope);
}
