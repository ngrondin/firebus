package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.values.abs.SValue;

public class Expression extends Executor {
	protected Scope scope;
	protected ExecutionUnit rootExecutionUnit;
	protected boolean fullExceptions = false;
	
	protected Expression(Scope s, ExecutionUnit eu) {
		rootExecutionUnit = eu;
		scope = s;
	}
	
	public Object eval(Map<String, Object> context) throws ScriptException {
		Scope localScope = new Scope(scope);
		for(String key: context.keySet())
			localScope.declareValue(key, Converter.convertIn(context.get(key)));
		SValue ret = null;
		try {
			ret = rootExecutionUnit.eval(localScope);
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
