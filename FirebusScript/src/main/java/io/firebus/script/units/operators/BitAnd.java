package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoIntegerOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;

public class BitAnd extends TwoIntegerOperator {

	public BitAnd(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithInts(int i1, int i2) throws ScriptExecutionException {
		return new SNumber(i1 & i2);
	}
}
