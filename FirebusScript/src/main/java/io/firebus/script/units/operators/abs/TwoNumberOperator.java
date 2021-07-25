package io.firebus.script.units.operators.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public abstract class TwoNumberOperator extends TwoExpressionOperator {

	public TwoNumberOperator(Expression e1, Expression e2, UnitContext uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptException {
		if(v1 instanceof SNumber && v2 instanceof SNumber) {
			Number n1 = ((SNumber)v1).getNumber();
			Number n2 = ((SNumber)v2).getNumber();
			return evalWithNumbers(n1, n2);
		} else {
			throw new ScriptException(this.getClass().getSimpleName() + " operator requries 2 numeric value", context);
		}
	}

	protected abstract SValue evalWithNumbers(Number n1, Number n2) throws ScriptException;

}
