package io.firebus.script.units.operators;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SSkipExpression;

public class LogicalAnd extends Operator {
	protected Expression expr1;
	protected Expression expr2;
	
	public LogicalAnd(Expression e1, Expression e2, SourceInfo uc) {
		super(uc);
		expr1 = e1;
		expr2 = e2;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		try {
			SValue v1 = expr1.eval(scope);
			if(v1 instanceof SSkipExpression) return v1;
			if(v1.toBoolean() == true) {
				SValue v2 = expr2.eval(scope);
				if(v2 instanceof SSkipExpression) return v2;
				if(v2.toBoolean() == true) {
					return SBoolean.get(true);
				} else {
					return SBoolean.get(false);
				}
			} else {
				return SBoolean.get(false);
			}
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage() + " in '" + this.toString() + "'", source);
		}
	}
}
