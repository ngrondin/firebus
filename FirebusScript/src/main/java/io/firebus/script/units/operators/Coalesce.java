package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoExpressionOperator;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SValue;

public class Coalesce extends TwoExpressionOperator {

	public Coalesce(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptException {
		return v1 instanceof SNull ? v2 : v1;
	}


}
