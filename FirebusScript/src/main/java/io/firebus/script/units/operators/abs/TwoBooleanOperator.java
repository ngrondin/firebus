package io.firebus.script.units.operators.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.units.Expression;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SValue;

public abstract class TwoBooleanOperator extends TwoExpressionOperator {

	public TwoBooleanOperator(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptException {
		if(v1 instanceof SBoolean && v2 instanceof SBoolean) {
			boolean b1 = ((SBoolean)v1).getBoolean();
			boolean b2 = ((SBoolean)v2).getBoolean();
			return evalWithBools(b1, b2);
		} else {
			throw new ScriptException(this.getClass().getSimpleName() + " operator requries 2 boolean value", source);
		}
	}

	protected abstract SValue evalWithBools(boolean b1, boolean b2) throws ScriptException;
}
