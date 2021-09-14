package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.tools.Operations;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoExpressionOperator;
import io.firebus.script.values.abs.SValue;

public class Substract extends TwoExpressionOperator {

	public Substract(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptException {
		try {
			return Operations.substract(v1, v2);
		} catch(ScriptException e) {
			throw new ScriptException(e.getMessage(), source);
		}
	}
}
