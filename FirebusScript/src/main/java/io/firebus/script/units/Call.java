package io.firebus.script.units;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.objects.Callable;
import io.firebus.script.objects.ScriptObject;
import io.firebus.script.scopes.Scope;

public class Call extends Expression {
	protected Expression callableExpression;
	protected List<Expression> paramExpressions;
	
	public Call(Expression c, List<Expression> e) {
		callableExpression = c;
		paramExpressions = e;
	}

	public ScriptObject eval(Scope scope) {
		ScriptObject c = callableExpression.eval(scope);
		if(c instanceof Callable) {
			List<ScriptObject> params = new ArrayList<ScriptObject>();
			for(Expression e : paramExpressions) {
				params.add(e.eval(scope));
			}
			ScriptObject ret = ((Callable)c).call(params);
			return ret;
		} else {
			return null; //Throw exception
		}
	}

}
