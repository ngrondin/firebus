package io.firebus.script.units.operators;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SValue;

public class LogicalAnd extends Operator {
	protected Expression expr1;
	protected Expression expr2;
	
	public LogicalAnd(Expression e1, Expression e2, SourceInfo uc) {
		super(uc);
		expr1 = e1;
		expr2 = e2;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue v1 = expr1.eval(scope);
		if(v1 instanceof SBoolean) {
			SBoolean b1 = (SBoolean)v1;
			if(b1.getBoolean() == true) {
				SValue v2 = expr2.eval(scope);
				if(v2 instanceof SBoolean) {
					SBoolean b2 = (SBoolean)v2;
					if(b2.getBoolean() == true) {
						return new SBoolean(true);
					} else {
						return new SBoolean(false);
					}
				} else {
					throw new ScriptException("'" + expr2.toString() + "' is not a boolean expression", source);
				}
			} else {
				return new SBoolean(false);
			}
		} else {
			throw new ScriptException("'" + expr1.toString() + "' is not a boolean expression", source);
		}
	}
}
