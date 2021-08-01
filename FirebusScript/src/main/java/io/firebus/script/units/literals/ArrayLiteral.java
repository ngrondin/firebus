package io.firebus.script.units.literals;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Literal;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SValue;

public class ArrayLiteral extends Literal {
	protected List<Expression> expressions;
	
	public ArrayLiteral(SourceInfo uc) {
		super(uc);
		expressions = new ArrayList<Expression>();
	}
	
	public void addExpression(Expression e) {
		expressions.add(e);
	}

	public SValue eval(Scope scope) throws ScriptException {
		List<SValue> list = new ArrayList<SValue>();
		for(Expression expr : expressions) {
			list.add(expr.eval(scope));
		}
		return new SArray(list);
	}

}
