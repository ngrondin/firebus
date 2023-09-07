package io.firebus.script.units.setters;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SSkipExpression;

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
		if(val instanceof SSkipExpression) val = SNull.get();
		ref.setValue(scope, val);
		return val;
	}

}
