package io.firebus.script.units.references;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.VariableId;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.values.abs.SValue;

public class VariableReference extends Reference {
	protected VariableId key;

	public VariableReference(String n, SourceInfo uc) {
		super(uc);
		key = new VariableId(n);
	}
	
	/*public String getName() {
		return key;
	}*/
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		return scope.getValue(key);
	}

	public void setValue(Scope scope, SValue val) throws ScriptExecutionException {
		scope.setValue(key, val);
	}
}
