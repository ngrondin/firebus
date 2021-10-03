package io.firebus.script.units.expressions;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.abs.SValue;

public class TernaryExpression extends Expression {

	protected Expression condition;
	protected Expression thenExpr;
	protected Expression elseExpr;
	
	public TernaryExpression(Expression c, Expression t, Expression e, SourceInfo uc) {
		super(uc);
		condition = c;
		thenExpr = t;
		elseExpr = e;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue res = condition.eval(scope);
		try {
			if(res.toBoolean() == true) 
				return thenExpr.eval(scope);
			else
				return elseExpr.eval(scope);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException("Ternary condition (" + res.toString() + ") cannot be evaluated to boolean", source);
		}
	}
}
