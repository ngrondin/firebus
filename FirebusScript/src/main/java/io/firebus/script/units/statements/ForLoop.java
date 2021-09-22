package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.setters.DeclareList;
import io.firebus.script.units.statements.abs.ConditionalIterator;

public class ForLoop extends ConditionalIterator {
	protected DeclareList declares;
	protected Expression initial;
	protected Operator operator;
	
	public ForLoop(DeclareList d, Expression c, Operator o, ExecutionUnit eu, SourceInfo uc) {
		super(c, eu, uc);
		declares = d;
		operator = o;
	}
	
	public ForLoop(Expression i, Expression c, Operator o, ExecutionUnit eu, SourceInfo uc) {
		super(c, eu, uc);
		initial = i;
		operator = o;
	}

	protected void before(Scope scope) throws ScriptExecutionException {
		if(declares != null)
			declares.eval(scope);
		else if(initial != null)
			initial.eval(scope);
	}

	protected void afterIteration(Scope scope) throws ScriptExecutionException {
		operator.eval(scope);
	}

}
