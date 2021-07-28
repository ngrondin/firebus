package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoBooleanOperator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SValue;

public class LogicalAnd extends TwoBooleanOperator {

	public LogicalAnd(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithBools(boolean b1, boolean b2) throws ScriptException {
		return new SBoolean(b1 && b2);
	}
}
