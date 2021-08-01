package io.firebus.script.units.references;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SValue;

public class VariableReference extends Reference {
	protected String name;

	public VariableReference(String n, SourceInfo uc) {
		super(uc);
		name = n;
	}
	
	public String getName() {
		return name;
	}
	
	public SValue eval(Scope scope) {
		return scope.getValue(name);
	}

	public void setValue(Scope scope, SValue val) throws ScriptException {
		Scope varScope = scope.getScopeOf(name);
		if(varScope != null) {
			varScope.setValue(name, val);
		} else {
			scope.setValue(name, val);
		}
	}
}