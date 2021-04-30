package io.firebus.script.units;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class Declare extends ExecutionUnit {
	protected String modifier;
	protected String key;
	protected Expression expression;
	
	public Declare(String k, Expression exp) {
		key = k;
		expression = exp;
	}
	
	public void setModifier(String m) {
		modifier = m;
	}

	public SValue eval(Scope scope) {
		SValue val = expression.eval(scope);
		scope.setValue(key, val);
		return val;
	}
}
