package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoIntegerOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public class BitShiftRight extends TwoIntegerOperator {

	public BitShiftRight(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithInts(int i1, int i2) throws ScriptException {
		return new SNumber(i1 >> i2);
	}


}
