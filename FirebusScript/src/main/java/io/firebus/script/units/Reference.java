package io.firebus.script.units;

import io.firebus.script.objects.ScriptObject;
import io.firebus.script.scopes.Scope;

public class Reference extends Expression {
	protected String name;
	
	public Reference(String n) {
		name = n;
	}

	public ScriptObject eval(Scope scope) {
		return scope.getValue(name);
	}
}
