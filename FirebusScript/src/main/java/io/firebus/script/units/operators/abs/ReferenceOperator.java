package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.abs.SValue;

public abstract class ReferenceOperator extends Operator {
	protected Reference ref;

	public ReferenceOperator(Reference r, SourceInfo uc) {
		super(uc);
		ref = r;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue originalValue = ref.eval(scope);
		SValue updateValue = getUpdateValue(originalValue);
		SValue returnValue = getReturnValue(originalValue, updateValue);
		ref.setValue(scope, updateValue);
		return returnValue;
	}
	
	protected abstract SValue getUpdateValue(SValue originalValue) throws ScriptExecutionException;
	
	protected abstract SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptExecutionException;
}
