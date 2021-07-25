package io.firebus.script.units.operators.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SValue;

public abstract class OneIntegerOperator extends OneNumberOperator {

	public OneIntegerOperator(Expression e, UnitContext uc) {
		super(e, uc);
	}

	protected SValue evalWithNumber(Number n) throws ScriptException {
		int i = n.intValue();
		if(n.doubleValue() == i) {
			return evalWithInt(i);
		} else {
			throw new ScriptException(this.getClass().getSimpleName() + " operator requries an integer", context);
		}
	}
	
	protected abstract SValue evalWithInt(int i) throws ScriptException;

}
