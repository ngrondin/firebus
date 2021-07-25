package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.TwoBooleanOperator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SValue;

public class LogicalOr extends TwoBooleanOperator {

	public LogicalOr(Expression e1, Expression e2, UnitContext uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithBools(boolean b1, boolean b2) throws ScriptException {
		return new SBoolean(b1 || b2);
	}

}
