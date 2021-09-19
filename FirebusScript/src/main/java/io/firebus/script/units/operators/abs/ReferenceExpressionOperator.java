package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.abs.SValue;

public abstract class ReferenceExpressionOperator extends Operator {
	protected Reference ref;
	protected Expression expr;
	
	public ReferenceExpressionOperator(Reference r, Expression e, SourceInfo uc) {
		super(uc);
		ref = r;
		expr = e;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue originalValue = ref.eval(scope);
		SValue expressionValue = expr.eval(scope);
		SValue updateValue = getUpdateValue(originalValue, expressionValue);
		SValue returnValue = getReturnValue(originalValue, updateValue);
		ref.setValue(scope, updateValue);
		return returnValue;
	}

	protected abstract SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptExecutionException;

	protected abstract SValue getUpdateValue(SValue originalValue, SValue expressionValue) throws ScriptExecutionException;
}
