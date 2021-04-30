package io.firebus.script.units;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class Reference extends Expression {
	protected String name;
	
	public Reference(String n) {
		name = n;
	}

	public SValue eval(Scope scope) {
		return scope.getValue(name);
	}
}
