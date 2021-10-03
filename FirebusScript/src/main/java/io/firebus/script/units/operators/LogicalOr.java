package io.firebus.script.units.operators;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;

public class LogicalOr extends Operator {
	protected Expression expr1;
	protected Expression expr2;
	
	public LogicalOr(Expression e1, Expression e2, SourceInfo uc) {
		super(uc);
		expr1 = e1;
		expr2 = e2;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue v1 = expr1.eval(scope);
		if(v1 instanceof SNull) {
			return expr2.eval(scope);
		} else if(v1 instanceof SBoolean && ((SBoolean)v1).getBoolean() == false) {
			return expr2.eval(scope);
		} else {
			return v1;
		}
	}
}
