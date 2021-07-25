package io.firebus.script.units.operators.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SValue;

public abstract class TwoExpressionOperator extends Operator {
	protected Expression expr1;
	protected Expression expr2;

	public TwoExpressionOperator(Expression e1, Expression e2, UnitContext uc) {
		super(uc);
		expr1 = e1;
		expr2 = e2;
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		SValue v1 = expr1.eval(scope);
		SValue v2 = expr2.eval(scope);
		return evalWithValues(v1, v2);
	}
	
	protected abstract SValue evalWithValues(SValue v1, SValue v2) throws ScriptException;
}