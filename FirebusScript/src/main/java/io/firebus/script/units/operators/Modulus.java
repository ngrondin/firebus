package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.tools.Operations;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.TwoExpressionOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;

public class Modulus extends TwoExpressionOperator {

	public Modulus(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithInts(int i1, int i2) throws ScriptExecutionException {
		return new SNumber(i1 % i2);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptExecutionException, ScriptValueException {
		return Operations.modulus(v1, v2);
	}

}
