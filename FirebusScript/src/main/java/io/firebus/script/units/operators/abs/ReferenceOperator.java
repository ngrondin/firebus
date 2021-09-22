package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.abs.SValue;

public abstract class ReferenceOperator extends ValueOperator {
	protected Reference ref;

	public ReferenceOperator(Reference r, SourceInfo uc) {
		super(uc);
		ref = r;
	}
	
	protected SValue valueOpEval(Scope scope) throws ScriptExecutionException, ScriptValueException {
		SValue originalValue = ref.eval(scope);
		SValue updateValue = getUpdateValue(originalValue);
		SValue returnValue = getReturnValue(originalValue, updateValue);
		ref.setValue(scope, updateValue);
		return returnValue;
	}
	
	protected abstract SValue getUpdateValue(SValue originalValue) throws ScriptExecutionException, ScriptValueException;
	
	protected abstract SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptExecutionException, ScriptValueException;
}
