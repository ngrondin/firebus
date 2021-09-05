package io.firebus.script.units.operators;


import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.references.VariableReference;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SInternalObject;
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

	public SValue eval(Scope scope) throws ScriptException {
		SObject o = null;
		SValue[] arguments = new SValue[paramExpressions.size()];
		for(int i = 0; i < paramExpressions.size(); i++)
			arguments[i] = paramExpressions.get(i).eval(scope);
		
		if(callableExpression instanceof VariableReference) {
			String identifier = ((VariableReference)callableExpression).getName();
			if(identifier.equals("Date")) {
				o = new SDate(arguments);
			}
		}

		if(o == null) {
			o = new SInternalObject();
			SValue c = callableExpression.eval(scope);
			if(c instanceof SContextCallable) {
				SContextCallable callable = (SContextCallable)c;
				callable.call(o, arguments);
			} else {
				throw new ScriptException("New operator requires a context callable", source);
			}			
		}
		return o;
	}
}
