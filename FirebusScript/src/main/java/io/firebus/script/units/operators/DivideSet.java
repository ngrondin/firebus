package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.tools.Operations;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.ReferenceExpressionOperator;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.abs.SValue;

public class DivideSet extends ReferenceExpressionOperator {
	
	public DivideSet(Reference r, Expression e, SourceInfo uc) {
		super(r, e, uc);
	}

	protected SValue getUpdateValue(SValue originalValue, SValue expressionValue) throws ScriptExecutionException {
		try {
			return Operations.divide(originalValue, expressionValue);
		} catch(ScriptException e) {
			throw new ScriptExecutionException(e.getMessage(), source);
		}
	}
	
	protected SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptExecutionException {
		return updatedValue;
	}

}
