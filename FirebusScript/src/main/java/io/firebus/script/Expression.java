package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.values.abs.SValue;

public class Expression {
	protected Scope scope;
	protected ExecutionUnit rootExecutionUnit;
	
	protected Expression(Scope s, ExecutionUnit eu) {
		rootExecutionUnit = eu;
		scope = s;
	}
	
	public Object eval(Map<String, Object> context) throws ScriptException {
		Scope localScope = new Scope(scope);
		for(String key: context.keySet())
			localScope.setValue(new VariableId(key), Converter.convertIn(context.get(key)));
		SValue ret = rootExecutionUnit.eval(localScope);
		return Converter.convertOut(ret);
	}
}
