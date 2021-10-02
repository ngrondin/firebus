package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class TwoIntegerOperator extends TwoNumberOperator {

	public TwoIntegerOperator(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	@Override
	protected SValue evalWithNumbers(Number n1, Number n2) throws ScriptExecutionException {
		long i1 = n1.longValue();
		long i2 = n2.longValue();
		if(n1.doubleValue() == i1 && n2.doubleValue() == i2) {
			return evalWithInts(i1, i2);
		} else {
			throw new ScriptExecutionException(this.getClass().getSimpleName() + " operator requries 2 integers", source);
		}
	}
	
	protected abstract SValue evalWithInts(long i1, long i2) throws ScriptExecutionException;

}
