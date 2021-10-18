package io.firebus.script;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SReturn;

public class Program extends Executor {
	protected Scope defaultScope;
	protected ExecutionUnit rootExecutionUnit;
	protected boolean fullExceptions = false;
	
	protected Program(Scope s, ExecutionUnit eu) {
		rootExecutionUnit = eu;
		defaultScope = s;
	}
	
	public Object run() throws ScriptException {
		Scope executionScope = new Scope(defaultScope);
		return execute(executionScope);
	}
	
	public Object run(ScriptContext context) throws ScriptException {
		return execute(context.getScope());
	}
	
	protected Object execute(Scope executionScope) throws ScriptException {
		SValue ret = null;
		try {
			rootExecutionUnit.eval(executionScope);
		} catch(ScriptExecutionException e) {
 			if(fullExceptions) {
 				throw e;
 			} else {
 				throw ScriptException.flatten(e, Program.class);
 			}
		}
		if(ret instanceof SReturn)
			return Converter.convertOut(((SReturn)ret).getReturnedValue());
		else
			return null;
	}
}
