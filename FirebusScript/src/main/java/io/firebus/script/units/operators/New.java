package io.firebus.script.units.operators;


import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.values.SInternalCallable;
import io.firebus.script.values.SInternalObject;
import io.firebus.script.values.SMemberCallable;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class New extends Operator {
	protected Expression callableExpression;
	protected List<Expression> paramExpressions;

	
	public New(Expression ce, List<Expression> p, SourceInfo uc) {
		super(uc);
		callableExpression = ce;
		paramExpressions = p;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SInternalObject o = new SInternalObject();
		SValue c = callableExpression.eval(scope);
		if(c instanceof SCallable) {
			SCallable callable = (SCallable)c;
			if(callable instanceof SInternalCallable)
				callable = new SMemberCallable(o, callable);
			SValue[] arguments = new SValue[paramExpressions.size()];
			for(int i = 0; i < paramExpressions.size(); i++)
				arguments[i] = paramExpressions.get(i).eval(scope);
			callable.call(arguments);
			return o;
		} else {
			throw new ScriptException("New operator required a callable", source);
		}
	}

}
