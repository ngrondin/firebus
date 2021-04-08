package io.firebus.script.units;

import io.firebus.script.objects.ScriptObject;
import io.firebus.script.scopes.Scope;

public class Setter extends ExecutionUnit {
	protected String key;
	protected Expression expression;
	
	public Setter(String k, Expression exp) {
		key = k;
		expression = exp;
	}

	public ScriptObject eval(Scope scope) {
		ScriptObject val = expression.eval(scope);
		scope.setValue(key, val);
		return val;
	}

}
