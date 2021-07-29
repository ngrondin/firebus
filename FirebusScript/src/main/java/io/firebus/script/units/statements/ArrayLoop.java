package io.firebus.script.units.statements;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Declare;
import io.firebus.script.units.DeclareList;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.statements.abs.Iterator;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SValue;

public class ArrayLoop extends Iterator {
	protected DeclareList declares;
	protected Expression arrayExpr;
	protected SArray array;
	protected int index;

	public ArrayLoop(DeclareList d, Expression a, ExecutionUnit eu, SourceInfo uc) {
		super(eu, uc);
		declares = d;
		arrayExpr = a;
	}

	protected void before(Scope scope) throws ScriptException {
		SValue a = arrayExpr.eval(scope);
		if(a instanceof SArray) {
			array = (SArray)a;
			index = 0;
			for(Declare declare: declares.getDeclares()) {
				scope.setValue(declare.getKey(), array.get(index));
			}
		} else {
			throw new ScriptException("Expression must be an array", source);
		}
	}

	protected boolean continueLoop(Scope scope) throws ScriptException {
		return index < array.getSize();
	}

	protected void afterIteration(Scope scope) throws ScriptException {
		index++;
		if(index < array.getSize())
			for(Declare declare: declares.getDeclares())
				scope.setValue(declare.getKey(), array.get(index));
	}

}
