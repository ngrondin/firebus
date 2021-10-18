package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SReturn;

public class Function extends Executor {
	protected Scope defaultScope;
	protected String[] parameters;
	protected ExecutionUnit rootExecutionUnit;
	protected boolean fullExceptions = false;
	
	protected Function(Scope s, String[] p, ExecutionUnit eu) {
		rootExecutionUnit = eu;
		parameters = p;
		defaultScope = s;
	}
	
	public Object call(Map<String, Object> map) throws ScriptException {
		return execute(createExecutionScope(defaultScope, createArguments(map)));
	}
	
 	public Object call(Object ...arguments) throws ScriptException {
		return execute(createExecutionScope(defaultScope, arguments));
	}

	public Object call(ScriptContext context, Map<String, Object> map) throws ScriptException {
		return execute(createExecutionScope(context.getScope(), createArguments(map)));
	}
	
 	public Object call(ScriptContext context, Object ...arguments) throws ScriptException {
		return execute(createExecutionScope(context.getScope(), arguments));
	}

 	protected Object[] createArguments(Map<String, Object> map) {
 		Object[] arguments = new Object[parameters.length];
		for(int i = 0; i < arguments.length; i++) 
			arguments[i] = map.get(parameters[i]);
		return arguments;
 	}
	
 	protected Scope createExecutionScope(Scope parentScope, Object ...arguments) throws ScriptException {
		Scope executionScope = new Scope(parentScope);
		for(int i = 0; i < arguments.length; i++) {
			if(i < parameters.length) {
				executionScope.declareValue(parameters[i], Converter.convertIn(arguments[i]));
			}
		}
		return executionScope;
	}
 	
 	protected Object execute(Scope executionScope) throws ScriptException {
		SValue ret = null;
		try {
			ret = rootExecutionUnit.eval(executionScope);
		} catch(ScriptExecutionException e) {
 			if(fullExceptions) {
 				throw e;
 			} else {
 				throw ScriptException.flatten(e, Function.class);
 			}
		}
		if(ret instanceof SReturn)
			return Converter.convertOut(((SReturn)ret).getReturnedValue());
		else
			return null;
	}
}
