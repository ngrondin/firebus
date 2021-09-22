package io.firebus.script.units.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.values.abs.SValue;

public abstract class ExecutionUnit {
	protected SourceInfo source;
	
	public ExecutionUnit(SourceInfo uc) {
		source = uc;
	}
	
	public abstract SValue eval(Scope scope) throws ScriptExecutionException;
	
	public String toString() {
		return source.toString();
	}
}
