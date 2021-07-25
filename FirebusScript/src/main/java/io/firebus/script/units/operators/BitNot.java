package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.OneIntegerOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public class BitNot extends OneIntegerOperator {

	public BitNot(Expression e, UnitContext uc) {
		super(e, uc);
	}

	protected SValue evalWithInt(int i) throws ScriptException {
		return new SNumber(~i);
	}
}
