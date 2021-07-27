package io.firebus.script.units;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class PropertySetter extends Expression {
	protected String key;
	protected Expression expression;
	
	public PropertySetter(String k, Expression exp, UnitContext uc) {
		super(uc);
		key = k;
		expression = exp;
	}
	
	public String getKey() {
		return key;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue val = expression.eval(scope);
		return val;
	}

}
