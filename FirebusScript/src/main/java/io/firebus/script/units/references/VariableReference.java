package io.firebus.script.units.references;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SSkipExpression;

public class VariableReference extends Reference {
	protected String key;

	public VariableReference(String n, SourceInfo uc) {
		super(uc);
		key = n;
	}
	
	public String getName() {
		return key;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		return scope.getValue(key);
	}

	public void setValue(Scope scope, SValue val) throws ScriptExecutionException {
		SValue value = val;
		if(value instanceof SSkipExpression) value = SNull.get();
		scope.setValue(key, value);
	}
}
