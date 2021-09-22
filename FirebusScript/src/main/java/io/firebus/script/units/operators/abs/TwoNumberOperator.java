package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class TwoNumberOperator extends TwoExpressionOperator {

	public TwoNumberOperator(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptExecutionException {
		try {
			Number n1 = v1.toNumber();
			Number n2 = v2.toNumber();
			return evalWithNumbers(n1, n2);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage() + " in '" + this.toString() + "'", source);
		}
	}

	protected abstract SValue evalWithNumbers(Number n1, Number n2) throws ScriptExecutionException;

}
