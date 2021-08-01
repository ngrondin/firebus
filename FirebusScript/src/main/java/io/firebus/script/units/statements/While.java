package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.statements.abs.ConditionalIterator;

public class While extends ConditionalIterator {
	
	public While(Expression c, ExecutionUnit u, SourceInfo uc) {
		super(c, u, uc);
	}

	protected void before(Scope scope) throws ScriptException {
	}


	protected void afterIteration(Scope scope) throws ScriptException {
	}

}
