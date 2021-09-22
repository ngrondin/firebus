package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SReturn;

public class Function {
	protected Scope scope;
	protected String[] parameters;
	protected ExecutionUnit rootExecutionUnit;
	
	protected Function(Scope s, String[] p, ExecutionUnit eu) {
		rootExecutionUnit = eu;
		parameters = p;
		scope = s;
	}
	
	public Object call(Map<String, Object> map) throws ScriptException {
		Object[] args = new Object[parameters.length];
		for(int i = 0; i < args.length; i++) 
			args[i] = map.get(parameters[i]);
		return call(args);
	}
	
 	public Object call(Object ...arguments) throws ScriptException {
		Scope localScope = new Scope(scope);
		for(int i = 0; i < arguments.length; i++) {
			if(i < parameters.length) {
				localScope.setValue(parameters[i], Converter.convertIn(arguments[i]));
			}
		}
		SValue ret = rootExecutionUnit.eval(localScope);
		if(ret instanceof SReturn)
			return Converter.convertOut(((SReturn)ret).getReturnedValue());
		else
			return null;
	}
}
