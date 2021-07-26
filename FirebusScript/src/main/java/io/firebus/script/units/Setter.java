package io.firebus.script.units;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class Setter extends Expression {
	protected String key;
	protected Expression expression;
	
	public Setter(String k, Expression exp, UnitContext uc) {
		super(uc);
		key = k;
		expression = exp;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue val = expression.eval(scope);
		Scope varScope = scope.getScopeOf(key);
		if(varScope != null) {
			varScope.setValue(key, val);
		} else {
			scope.setValue(key, val);
		}
		return val;
	}

}
