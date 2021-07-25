package io.firebus.script.units.operators.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SValue;

public abstract class TwoIntegerOperator extends TwoNumberOperator {

	public TwoIntegerOperator(Expression e1, Expression e2, UnitContext uc) {
		super(e1, e2, uc);
	}

	@Override
	protected SValue evalWithNumbers(Number n1, Number n2) throws ScriptException {
		int i1 = n1.intValue();
		int i2 = n2.intValue();
		if(n1.doubleValue() == i1 && n2.doubleValue() == i2) {
			return evalWithInts(i1, i2);
		} else {
			throw new ScriptException(this.getClass().getSimpleName() + " operator requries 2 integers", context);
		}
	}
	
	protected abstract SValue evalWithInts(int i1, int i2) throws ScriptException;

}