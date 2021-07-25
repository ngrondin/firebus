package io.firebus.script.units.statements;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Statement;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SValue;
import io.firebus.script.values.flow.SReturn;

public class Return extends Statement {
	protected Expression expr;
	
	public Return(Expression e, UnitContext uc) {
		super(uc);
		expr = e;
	}

	public SValue eval(Scope scope) throws ScriptException {
		return new SReturn(expr != null ? expr.eval(scope) : null);
	}

}