package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class TwoBooleanOperator extends TwoExpressionOperator {

	public TwoBooleanOperator(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptExecutionException {
		try {
			boolean b1 = v1.toBoolean();
			boolean b2 = v2.toBoolean();
			return evalWithBools(b1, b2);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(this.getClass().getSimpleName() + " operator requries 2 boolean value", source);
		}
	}

	protected abstract SValue evalWithBools(boolean b1, boolean b2) throws ScriptExecutionException;
}
