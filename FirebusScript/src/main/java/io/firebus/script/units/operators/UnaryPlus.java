package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.OneNumberOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;

public class UnaryPlus extends OneNumberOperator {
	
	public UnaryPlus(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithNumber(Number n) throws ScriptExecutionException {
		return new SNumber(n);
	}
}
