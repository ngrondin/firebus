package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SValue;

public abstract class OneBooleanOperator extends OneExpressionOperator {

	public OneBooleanOperator(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithValue(SValue v) throws ScriptException {
		if(v instanceof SBoolean ) {
			boolean b = ((SBoolean)v).getBoolean();
			return evalWithBoolean(b);
		} else {
			throw new ScriptException(this.getClass().getSimpleName() + " operator requries 1 boolean value", source);
		}
	}

	protected abstract SValue evalWithBoolean(boolean b) throws ScriptException;

}
