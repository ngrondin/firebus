package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.values.SValue;

public abstract class OneExpressionOperator extends Operator {
	protected Expression expr;

	public OneExpressionOperator(Expression e, SourceInfo uc) {
		super(uc);
		expr = e;
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		SValue v = expr.eval(scope);
		return evalWithValue(v);
	}
	
	protected abstract SValue evalWithValue(SValue v) throws ScriptException;
}
