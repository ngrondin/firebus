package io.firebus.script.units.operators.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public abstract class OneNumberOperator extends OneExpressionOperator {

	public OneNumberOperator(Expression e, UnitContext uc) {
		super(e, uc);
	}

	protected SValue evalWithValue(SValue v) throws ScriptException {
		if(v instanceof SNumber) {
			Number n = ((SNumber)v).getNumber();
			return evalWithNumber(n);
		} else {
			throw new ScriptException(this.getClass().getSimpleName() + " operator requries 1 numeric value", context);
		}
	}

	protected abstract SValue evalWithNumber(Number n) throws ScriptException;

}
