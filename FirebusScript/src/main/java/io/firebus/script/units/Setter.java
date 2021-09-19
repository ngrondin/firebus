package io.firebus.script.units;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.abs.SValue;

public class Setter extends Expression {
	protected Reference ref;
	protected Expression expression;
	
	public Setter(Reference r, Expression exp, SourceInfo uc) {
		super(uc);
		ref = r;
		expression = exp;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue val = expression.eval(scope);
		ref.setValue(scope, val);
		return val;
	}

}
