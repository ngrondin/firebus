package io.firebus.script.units.operators.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Reference;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SValue;

public abstract class ReferenceOperator extends Operator {
	protected Reference ref;

	public ReferenceOperator(Reference r, UnitContext uc) {
		super(uc);
		ref = r;
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		SValue originalValue = ref.eval(scope);
		SValue updateValue = getUpdateValue(originalValue);
		SValue returnValue = getReturnValue(originalValue, updateValue);
		Scope targetScope = scope.getScopeOf(ref.getName());
		targetScope.setValue(ref.getName(), updateValue);
		return returnValue;
	}
	
	protected abstract SValue getUpdateValue(SValue originalValue) throws ScriptException;
	
	protected abstract SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptException;
}
