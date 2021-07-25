package io.firebus.script.units;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class Reference extends Expression {
	protected String name;
	
	public Reference(String n, UnitContext uc) {
		super(uc);
		name = n;
	}
	
	public String getName() {
		return name;
	}

	public SValue eval(Scope scope) {
		return scope.getValue(name);
	}
}
