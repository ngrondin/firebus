package io.firebus.script.units;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SCallable;
import io.firebus.script.values.SValue;

public class Call extends Expression {
	protected Expression callableExpression;
	protected List<Expression> paramExpressions;
	
	public Call(Expression c, List<Expression> e, UnitContext uc) {
		super(uc);
		callableExpression = c;
		paramExpressions = e;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue c = callableExpression.eval(scope);
		if(c != null) {
			if(c instanceof SCallable) {
				List<SValue> params = new ArrayList<SValue>();
				for(Expression e : paramExpressions) {
					params.add(e.eval(scope));
				}
				SValue ret = ((SCallable)c).call(params);
				return ret;
			} else {
				throw new ScriptException("Not a callable", context);
			}			
		} else {
			throw new ScriptException("Unknown function reference", context);
		}
	}

}
