package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptInternalException;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Statement;
import io.firebus.script.values.SValue;

public class Throw extends Statement {
	protected Expression expr;
	
	public Throw(Expression e, SourceInfo uc) {
		super(uc);
		expr = e;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue v = expr.eval(scope);
		throw new ScriptInternalException(v.toString());
	}

}
