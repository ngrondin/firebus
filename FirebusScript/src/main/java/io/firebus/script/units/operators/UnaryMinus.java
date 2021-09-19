package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.OneNumberOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;

public class UnaryMinus extends OneNumberOperator {

	public UnaryMinus(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithNumber(Number n) throws ScriptExecutionException {
		if(n instanceof Integer) {
			return new SNumber(-1 * n.intValue());
		} else {
			return new SNumber(-1 * n.doubleValue());
		}
	}
}
