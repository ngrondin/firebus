package io.firebus.script.units;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public abstract class ExecutionUnit {
	protected UnitContext context;
	
	public ExecutionUnit(UnitContext uc) {
		context = uc;
	}
	
	public abstract SValue eval(Scope scope) throws ScriptException;
}
