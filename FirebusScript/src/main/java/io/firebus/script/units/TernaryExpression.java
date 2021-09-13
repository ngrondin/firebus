package io.firebus.script.units;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SBoolean;
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

	public SValue eval(Scope scope) throws ScriptException {
		SValue res = condition.eval(scope);
		if(res instanceof SBoolean) {
			boolean r = ((SBoolean)res).getBoolean();
			if(r) 
				return thenExpr.eval(scope);
			else
				return elseExpr.eval(scope);
		} else {
			throw new ScriptException("Ternary condition must return a boolean");
		}
	}
}
