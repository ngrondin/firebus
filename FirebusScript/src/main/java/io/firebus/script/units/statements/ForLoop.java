package io.firebus.script.units.statements;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.DeclareList;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.statements.abs.ConditionalIterator;

public class ForLoop extends ConditionalIterator {
	protected DeclareList declares;
	protected Operator operator;
	
	public ForLoop(DeclareList d, Expression c, Operator o, ExecutionUnit eu, UnitContext uc) {
		super(c, eu, uc);
		declares = d;
		operator = o;
	}

	protected void updateLocalScope(Scope scope) throws ScriptException {
		declares.eval(scope);
	}

	protected void afterIteration(Scope scope) throws ScriptException {
		operator.eval(scope);
	}

}
