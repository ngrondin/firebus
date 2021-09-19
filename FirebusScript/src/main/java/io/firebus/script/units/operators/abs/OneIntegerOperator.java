package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class OneIntegerOperator extends OneNumberOperator {

	public OneIntegerOperator(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithNumber(Number n) throws ScriptExecutionException {
		int i = n.intValue();
		if(n.doubleValue() == i) {
			return evalWithInt(i);
		} else {
			throw new ScriptExecutionException(this.getClass().getSimpleName() + " operator requries an integer", source);
		}
	}
	
	protected abstract SValue evalWithInt(int i) throws ScriptExecutionException;

}
