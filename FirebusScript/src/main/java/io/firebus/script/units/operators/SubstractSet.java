package io.firebus.script.units.operators;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;

public class SubstractSet extends Operator {
	
	protected Reference ref;
	protected Expression expression;
	
	public SubstractSet(Reference r, Expression exp, SourceInfo uc) {
		super(uc);
		ref = r;
		expression = exp;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue v1 = ref.eval(scope);
		SValue v2 = expression.eval(scope);
		
		throw new ScriptException("Not implemented");
	}

}
