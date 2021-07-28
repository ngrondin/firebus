package io.firebus.script.units.references;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class VariableReference extends KeyBasedReference {

	public VariableReference(String n, SourceInfo uc) {
		super(n, uc);
		
	}
	
	public SValue eval(Scope scope) {
		return scope.getValue(key);
	}

	public void setValue(Scope scope, SValue val) throws ScriptException {
		Scope varScope = scope.getScopeOf(getKey());
		if(varScope != null) {
			varScope.setValue(getKey(), val);
		} else {
			scope.setValue(getKey(), val);
		}
	}
}
