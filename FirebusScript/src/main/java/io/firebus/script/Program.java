package io.firebus.script;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.abs.ExecutionUnit;

public class Program {
	protected Scope scope;
	protected ExecutionUnit rootExecutionUnit;
	
	protected Program(Scope s, ExecutionUnit eu) {
		rootExecutionUnit = eu;
		scope = s;
	}
	
	public void run() throws ScriptException {
		Scope localScope = new Scope(scope);
		rootExecutionUnit.eval(localScope);
	}
}
