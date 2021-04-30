package io.firebus.script.units.operators;

import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.values.SValue;
import io.firebus.script.values.SString;

public class AddOperator extends Operator {
	protected ExecutionUnit expression1;
	protected ExecutionUnit expression2;
	
	public AddOperator(Expression exp1, Expression exp2) {
		expression1 = exp1;
		expression2 = exp2;
	}
	
	public SValue eval(Scope scope) {
		SValue v1 = expression1.eval(scope);
		SValue v2 = expression2.eval(scope);
		SValue ret = null;
		if(v1 instanceof SString && v2 instanceof SString) {
			ret = new SString(v1.toString() + v2.toString());
		}
		return ret;
	}
}
