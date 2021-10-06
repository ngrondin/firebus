package io.firebus.script;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;

public class Program extends Executor {
	protected Scope scope;
	protected ExecutionUnit rootExecutionUnit;
	protected boolean fullExceptions = false;
	
	protected Program(Scope s, ExecutionUnit eu) {
		rootExecutionUnit = eu;
		scope = s;
	}
	
	public void run() throws ScriptException {
		Scope localScope = new Scope(scope);
		try {
			rootExecutionUnit.eval(localScope);
		} catch(ScriptExecutionException e) {
 			if(fullExceptions) {
 				throw e;
 			} else {
 				throw ScriptException.flatten(e, Program.class);
 			}
		}
		
	}
}
