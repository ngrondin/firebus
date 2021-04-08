package io.firebus.script.units.operators;

import io.firebus.script.objects.ScriptObject;
import io.firebus.script.objects.ScriptString;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;

public class AddOperator extends Operator {
	protected ExecutionUnit expression1;
	protected ExecutionUnit expression2;
	
	public AddOperator(Expression exp1, Expression exp2) {
		expression1 = exp1;
		expression2 = exp2;
	}
	
	public ScriptObject eval(Scope scope) {
		ScriptObject v1 = expression1.eval(scope);
		ScriptObject v2 = expression2.eval(scope);
		ScriptObject ret = null;
		if(v1 instanceof ScriptString && v2 instanceof ScriptString) {
			ret = new ScriptString(v1.toString() + v2.toString());
		}
		return ret;
	}
}
