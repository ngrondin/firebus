package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;

public class FixedExpression extends Expression {
	protected Object fixedValue;

	protected FixedExpression(Object fv) {
		super(null, null);
		fixedValue = fv;
	}

	public Object eval(Map<String, Object> context) throws ScriptException {
		return fixedValue;
	}	
	
	public Object eval(ScriptContext context) throws ScriptException {
		return fixedValue;
	}
	
	public String toString() {
		return fixedValue.toString();
	}
}
