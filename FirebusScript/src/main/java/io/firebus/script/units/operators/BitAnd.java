package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.TwoIntegerOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public class BitAnd extends TwoIntegerOperator {

	public BitAnd(Expression e1, Expression e2, UnitContext uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithInts(int i1, int i2) throws ScriptException {
		return new SNumber(i1 & i2);
	}
}
