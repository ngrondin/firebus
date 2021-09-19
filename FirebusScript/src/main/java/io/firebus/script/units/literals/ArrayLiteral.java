package io.firebus.script.units.literals;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Literal;
import io.firebus.script.units.operators.Spread;
import io.firebus.script.values.SArray;
import io.firebus.script.values.abs.SValue;

public class ArrayLiteral extends Literal {
	protected List<Expression> expressions;
	
	public ArrayLiteral(SourceInfo uc) {
		super(uc);
		expressions = new ArrayList<Expression>();
	}
	
	public void addExpression(Expression e) {
		expressions.add(e);
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		List<SValue> list = new ArrayList<SValue>();
		for(Expression expr : expressions) {
			if(expr instanceof Spread) {
				SArray subArray = (SArray)((Spread)expr).eval(scope);
				for(int i = 0; i < subArray.getSize(); i++) {
					list.add(subArray.get(i));
				}
			} else {
				list.add(expr.eval(scope));
			}
		}
		return new SArray(list);
	}

}
