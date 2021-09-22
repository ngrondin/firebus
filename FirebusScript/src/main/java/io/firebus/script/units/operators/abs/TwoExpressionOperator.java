package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class TwoExpressionOperator extends ValueOperator {
	protected Expression expr1;
	protected Expression expr2;

	public TwoExpressionOperator(Expression e1, Expression e2, SourceInfo uc) {
		super(uc);
		expr1 = e1;
		expr2 = e2;
	}
	
	protected SValue valueOpEval(Scope scope) throws ScriptExecutionException, ScriptValueException {
		SValue v1 = expr1.eval(scope);
		SValue v2 = expr2.eval(scope);
		return evalWithValues(v1, v2);
	}
	
	protected abstract SValue evalWithValues(SValue v1, SValue v2) throws ScriptExecutionException, ScriptValueException;
}
