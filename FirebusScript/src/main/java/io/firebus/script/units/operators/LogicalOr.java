package io.firebus.script.units.operators;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.values.SBoolean;
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
		try {
			SValue v1 = expr1.eval(scope);
			boolean b1 = v1.toBoolean();
			if(b1 == false) {
				SValue v2 = expr2.eval(scope);
				boolean b2 = v2.toBoolean();
				if(b2 == false) {
					return new SBoolean(false);
				} else {
					return new SBoolean(true);
				}
			} else {
				return new SBoolean(true);
			}
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage() + " in '" + this.toString() + "'", source);
		}
	}
}
