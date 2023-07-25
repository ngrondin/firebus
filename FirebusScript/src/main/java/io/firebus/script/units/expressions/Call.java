package io.firebus.script.units.expressions;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SSkipExpression;

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
			for(int i = 0; i < paramExpressions.size(); i++) {
				SValue argVal = paramExpressions.get(i).eval(scope);
				if(argVal instanceof SSkipExpression) return argVal;
				arguments[i] = argVal;
			}
			try {
				SValue ret = ((SCallable)c).call(arguments);
				return ret;
			} catch(ScriptCallException e) {
				throw new ScriptExecutionException("Error calling '"+ callableExpression.toString() + "'", e, source);
			}
		} else if(c instanceof SSkipExpression) {
			return c;
		} else if(c instanceof SUndefined) {
			throw new ScriptExecutionException("'" + callableExpression.toString() + "' is undefined", source);
		} else if(c instanceof SNull) {
			throw new ScriptExecutionException("'" + callableExpression.toString() + "' is null", source);
		} else {
			throw new ScriptExecutionException("'" + callableExpression.toString() + "' is not a callable", source);
		}			
	}

}
