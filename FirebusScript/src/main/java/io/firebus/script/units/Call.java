package io.firebus.script.units;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SCallable;
import io.firebus.script.values.SValue;

public class Call extends Expression {
	protected Expression callableExpression;
	protected List<Expression> paramExpressions;
	
	public Call(Expression c, List<Expression> e, SourceInfo uc) {
		super(uc);
		callableExpression = c;
		paramExpressions = e;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue c = callableExpression.eval(scope);
		if(c != null) {
			if(c instanceof SCallable) {
				SValue[] arguments = new SValue[paramExpressions.size()];
				for(int i = 0; i < paramExpressions.size(); i++)
					arguments[i] = paramExpressions.get(i).eval(scope);
				SValue ret = ((SCallable)c).call(arguments);
				return ret;
			} else {
				throw new ScriptException("Not a callable", source);
			}			
		} else {
			throw new ScriptException("Unknown function reference", source);
		}
	}

}
