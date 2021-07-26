package io.firebus.script.units.statements;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.statements.abs.ConditionalIterator;

public class While extends ConditionalIterator {
	
	public While(Expression c, ExecutionUnit u, UnitContext uc) {
		super(c, u, uc);
	}

	protected void updateLocalScope(Scope scope) throws ScriptException {
	}


	protected void afterIteration(Scope scope) throws ScriptException {
	}

}
