package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class OneNumberOperator extends OneExpressionOperator {

	public OneNumberOperator(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithValue(SValue v) throws ScriptExecutionException {
		try {
			Number n = v.toNumber();
			return evalWithNumber(n);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage() + " in '" + this.toString() + "'", source);
		}
	}

	protected abstract SValue evalWithNumber(Number n) throws ScriptExecutionException;

}
