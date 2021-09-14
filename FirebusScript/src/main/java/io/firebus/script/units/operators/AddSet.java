package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.tools.Operations;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.ReferenceExpressionOperator;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.abs.SValue;

public class AddSet extends ReferenceExpressionOperator {

	public AddSet(Reference r, Expression e, SourceInfo uc) {
		super(r, e, uc);
	}

	protected SValue getUpdateValue(SValue originalValue, SValue expressionValue) throws ScriptException {
		try {
			return Operations.add(originalValue, expressionValue);
		} catch(ScriptException e) {
			throw new ScriptException(e.getMessage(), source);
		}
	}
	
	protected SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptException {
		return updatedValue;
	}


}
