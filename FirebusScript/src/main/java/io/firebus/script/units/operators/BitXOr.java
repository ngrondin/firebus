package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.tools.Operations;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoExpressionOperator;
import io.firebus.script.values.abs.SValue;

public class BitXOr extends TwoExpressionOperator {

	public BitXOr(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptExecutionException {
		try {
			return Operations.bitXor(v1, v2);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage(), source);
		}
	}

}
