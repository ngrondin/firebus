package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.OneNumberOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public class UnaryMinus extends OneNumberOperator {

	public UnaryMinus(Expression e, UnitContext uc) {
		super(e, uc);
	}

	protected SValue evalWithNumber(Number n) throws ScriptException {
		if(n instanceof Integer) {
			return new SNumber(-1 * n.intValue());
		} else {
			return new SNumber(-1 * n.doubleValue());
		}
	}
}
