package io.firebus.script.units.operators;


import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.values.SInternalObject;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SContextCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

public class New extends Operator {
	protected Expression callableExpression;
	protected List<Expression> paramExpressions;

	
	public New(Expression ce, List<Expression> p, SourceInfo uc) {
		super(uc);
		callableExpression = ce;
		paramExpressions = p;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SObject o = null;
		SValue[] arguments = new SValue[paramExpressions.size()];
		for(int i = 0; i < paramExpressions.size(); i++)
			arguments[i] = paramExpressions.get(i).eval(scope);

		SValue c = callableExpression.eval(scope);
		if(c instanceof SCallable) {
			try {
				if(c instanceof SContextCallable) {
					SContextCallable callable = (SContextCallable)c;
					o = new SInternalObject();
					callable.call(o, arguments);	
				} else {
					SCallable callable = (SCallable)c;
					SValue v = callable.call(arguments);
					if(v instanceof SObject) {
						o = (SObject)v;
					} else {
						throw new ScriptExecutionException("External callable used with a 'new' operator should return an object", source);
					}
				}
			} catch(ScriptCallException e) {
				throw new ScriptExecutionException("Error calling '" + this.toString() + "'", source);
			}
		} else {
			throw new ScriptExecutionException("'new' operator expects a callable", source);
		}
		return o;
	}
}
