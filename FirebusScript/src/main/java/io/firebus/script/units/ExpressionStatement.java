package io.firebus.script.units;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SValue;

public class ExpressionStatement extends Statement {
	protected Expression expression;
	
	public ExpressionStatement(Expression e, SourceInfo uc) {
		super(uc);
		expression = e;
	}

	public SValue eval(Scope scope) throws ScriptException {
		return expression.eval(scope);
	}

}
