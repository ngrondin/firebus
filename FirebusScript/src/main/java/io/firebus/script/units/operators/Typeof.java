package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.OneExpressionOperator;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;

public class Typeof extends OneExpressionOperator {
	
	public Typeof(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithValue(SValue v) throws ScriptExecutionException {
		return new SString(v.typeOf());
	}


}
