package io.firebus.script.units;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.SValue;

public class Setter extends Expression {
	protected Reference ref;
	protected Expression expression;
	
	public Setter(Reference r, Expression exp, SourceInfo uc) {
		super(uc);
		ref = r;
		expression = exp;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue val = expression.eval(scope);
		try {
			ref.setValue(scope, val);
		} catch(ScriptException e) {
			throw new ScriptException(e.getMessage(), source);
		}
		return val;
	}

}
