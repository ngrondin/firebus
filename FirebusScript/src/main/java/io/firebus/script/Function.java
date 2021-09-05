package io.firebus.script;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SReturn;

public class Function {
	protected Scope scope;
	protected String[] parameters;
	protected ExecutionUnit rootExecutionUnit;
	protected Converter converter;
	
	protected Function(Scope s, String[] p, ExecutionUnit eu, Converter c) {
		rootExecutionUnit = eu;
		parameters = p;
		scope = s;
		converter = c;
	}
	
	public Object call(Object ...arguments) throws ScriptException {
		Scope localScope = new Scope(scope);
		for(int i = 0; i < arguments.length; i++) {
			if(i < parameters.length) {
				localScope.setValue(parameters[i], converter.convertIn(arguments[i]));
			}
		}
		SValue ret = rootExecutionUnit.eval(localScope);
		if(ret instanceof SReturn)
			return converter.convertOut(((SReturn)ret).getReturnedValue());
		else
			return null;
	}
}
