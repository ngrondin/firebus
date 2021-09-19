package io.firebus.script.units;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Call extends Expression {
	protected Expression callableExpression;
	protected List<Expression> paramExpressions;
	
	public Call(Expression c, List<Expression> e, SourceInfo uc) {
		super(uc);
		callableExpression = c;
		paramExpressions = e;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue c = callableExpression.eval(scope);
		if(c instanceof SCallable) {
			SValue[] arguments = new SValue[paramExpressions.size()];
			for(int i = 0; i < paramExpressions.size(); i++)
				arguments[i] = paramExpressions.get(i).eval(scope);
			try {
				SValue ret = ((SCallable)c).call(arguments);
				return ret;
			} catch(ScriptCallException e) {
				throw new ScriptExecutionException("Error calling '"+ callableExpression.toString() + "'", source);
			}
		} else {
			throw new ScriptExecutionException("'" + callableExpression.toString() + "' is not a callable", source);
		}			
	}

}
