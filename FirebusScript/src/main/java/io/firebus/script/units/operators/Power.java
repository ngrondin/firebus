package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoNumberOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;

public class Power extends TwoNumberOperator {

	public Power(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithNumbers(Number n1, Number n2) throws ScriptExecutionException {
		double v = Math.pow(n1.doubleValue(), n2.doubleValue());
		return new SNumber(v);
	}

}
