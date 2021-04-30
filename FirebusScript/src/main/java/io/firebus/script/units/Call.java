package io.firebus.script.units;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SCallable;
import io.firebus.script.values.SValue;

public class Call extends Expression {
	protected Expression callableExpression;
	protected List<Expression> paramExpressions;
	
	public Call(Expression c, List<Expression> e) {
		callableExpression = c;
		paramExpressions = e;
	}

	public SValue eval(Scope scope) {
		SValue c = callableExpression.eval(scope);
		if(c instanceof SCallable) {
			List<SValue> params = new ArrayList<SValue>();
			for(Expression e : paramExpressions) {
				params.add(e.eval(scope));
			}
			SValue ret = ((SCallable)c).call(params);
			return ret;
		} else {
			return null; //Throw exception
		}
	}

}
