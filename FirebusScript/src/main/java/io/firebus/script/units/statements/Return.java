package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Statement;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SReturn;

public class Return extends Statement {
	protected Expression expr;
	
	public Return(Expression e, SourceInfo uc) {
		super(uc);
		expr = e;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		return new SReturn(expr != null ? expr.eval(scope) : null);
	}

}
