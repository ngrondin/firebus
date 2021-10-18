package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.values.abs.SValue;

public class Expression extends Executor {
	protected Scope defaultScope;
	protected ExecutionUnit rootExecutionUnit;
	protected boolean fullExceptions = false;
	
	protected Expression(Scope s, ExecutionUnit eu) {
		rootExecutionUnit = eu;
		defaultScope = s;
	}
	
	public Object eval(Map<String, Object> map) throws ScriptException {
		Scope executionScope = new Scope(defaultScope);
		for(String key: map.keySet())
			executionScope.declareValue(key, Converter.convertIn(map.get(key)));
		return eval(executionScope);
	}
	
	public Object eval(ScriptContext context) throws ScriptException {
		return eval(context.getScope());
	}

	
	protected Object eval(Scope executionScope) throws ScriptException {
		SValue ret = null;
		try {
			ret = rootExecutionUnit.eval(executionScope);
		} catch(ScriptExecutionException e) {
 			if(fullExceptions) {
 				throw e;
 			} else {
 				throw ScriptException.flatten(e, Expression.class);
 			}
		}
		return Converter.convertOut(ret);
	}
	
	public String toString() {
		return rootExecutionUnit.toString();
	}
}
