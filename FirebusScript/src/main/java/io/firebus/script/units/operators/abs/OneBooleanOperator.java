package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class OneBooleanOperator extends OneExpressionOperator {

	public OneBooleanOperator(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithValue(SValue v) throws ScriptExecutionException {
		try {
			boolean b = v.toBoolean();
			return evalWithBoolean(b);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(this.getClass().getSimpleName() + " operator requries 1 boolean value", source);
		}
	}

	protected abstract SValue evalWithBoolean(boolean b) throws ScriptExecutionException;

}
