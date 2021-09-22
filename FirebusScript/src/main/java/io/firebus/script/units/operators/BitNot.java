package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.tools.Operations;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.OneExpressionOperator;
import io.firebus.script.values.abs.SValue;

public class BitNot extends OneExpressionOperator {

	public BitNot(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithValue(SValue v1) throws ScriptExecutionException {
		try {
			return Operations.bitNot(v1);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage(), source);
		}
	}
}
