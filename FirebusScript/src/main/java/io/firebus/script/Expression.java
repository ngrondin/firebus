package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.values.abs.SValue;

public class Expression {
	protected Scope scope;
	protected ExecutionUnit rootExecutionUnit;
	protected Converter converter;
	
	protected Expression(Scope s, ExecutionUnit eu, Converter c) {
		rootExecutionUnit = eu;
		scope = s;
		converter = c;
	}
	
	public Object eval(Map<String, Object> context) throws ScriptException {
		Scope localScope = new Scope(scope);
		for(String key: context.keySet())
			localScope.setValue(key, converter.convertIn(context.get(key)));
		SValue ret = rootExecutionUnit.eval(localScope);
		return converter.convertOut(ret);
	}
}
