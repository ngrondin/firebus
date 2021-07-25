package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.OneNumberOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public class UnaryPlus extends OneNumberOperator {
	
	public UnaryPlus(Expression e, UnitContext uc) {
		super(e, uc);
	}

	protected SValue evalWithNumber(Number n) throws ScriptException {
		return new SNumber(n);
	}
}