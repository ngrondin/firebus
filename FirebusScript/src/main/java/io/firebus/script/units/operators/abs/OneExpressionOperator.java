package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SSkipExpression;

public abstract class OneExpressionOperator extends ValueOperator {
	protected Expression expr;

	public OneExpressionOperator(Expression e, SourceInfo uc) {
		super(uc);
		expr = e;
	}
	
	protected SValue valueOpEval(Scope scope) throws ScriptExecutionException, ScriptValueException {
		SValue val = expr.eval(scope);
		if(val instanceof SSkipExpression) return SSkipExpression.get();
		return evalWithValue(val);		
	}
	
	protected abstract SValue evalWithValue(SValue v) throws ScriptExecutionException, ScriptValueException;
}
