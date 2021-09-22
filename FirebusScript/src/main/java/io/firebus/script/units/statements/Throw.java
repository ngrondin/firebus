package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptInternalException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.values.abs.SValue;

public class Throw extends Statement {
	protected Expression expr;
	
	public Throw(Expression e, SourceInfo uc) {
		super(uc);
		expr = e;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue v = expr.eval(scope);
		Throwable t = new ScriptInternalException(v.toString());
		throw new ScriptExecutionException("Internally thrown exception", t, source);
	}

}
