package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SSkipExpression;

public abstract class ReferenceExpressionOperator extends ValueOperator {
	protected Reference ref;
	protected Expression expr;
	
	public ReferenceExpressionOperator(Reference r, Expression e, SourceInfo uc) {
		super(uc);
		ref = r;
		expr = e;
	}

	protected SValue valueOpEval(Scope scope) throws ScriptExecutionException, ScriptValueException {
		SValue originalValue = ref.eval(scope);
		SValue expressionValue = expr.eval(scope);
		if(expressionValue instanceof SSkipExpression) return SSkipExpression.get();
		SValue updateValue = getUpdateValue(originalValue, expressionValue);
		SValue returnValue = getReturnValue(originalValue, updateValue);
		ref.setValue(scope, updateValue);
		return returnValue;
	}

	protected abstract SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptExecutionException, ScriptValueException;

	protected abstract SValue getUpdateValue(SValue originalValue, SValue expressionValue) throws ScriptExecutionException, ScriptValueException;
}
