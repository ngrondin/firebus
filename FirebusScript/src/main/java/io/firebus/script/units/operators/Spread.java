package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.OneExpressionOperator;
import io.firebus.script.values.SArray;
import io.firebus.script.values.abs.SValue;

public class Spread extends OneExpressionOperator {

	public Spread(Expression e, SourceInfo uc) {
		super(e, uc);
		
	}

	protected SValue evalWithValue(SValue v) throws ScriptExecutionException {
		if(v instanceof SArray) {
			return v;
		} else {
			return new SArray();
		}
	}

}
