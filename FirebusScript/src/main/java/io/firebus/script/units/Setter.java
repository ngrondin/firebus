package io.firebus.script.units;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class Setter extends ExecutionUnit {
	protected String key;
	protected Expression expression;
	
	public Setter(String k, Expression exp) {
		key = k;
		expression = exp;
	}

	public SValue eval(Scope scope) {
		SValue val = expression.eval(scope);
		scope.setValue(key, val);
		return val;
	}

}
