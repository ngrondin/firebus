package io.firebus.script.units.statements.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SValue;

public abstract class ConditionalIterator extends Iterator {
	protected Expression condition;
	
	public ConditionalIterator(Expression c, ExecutionUnit u, SourceInfo uc) {
		super(u, uc);
		condition = c;
	}

	protected boolean continueLoop(Scope scope) throws ScriptExecutionException {
		SValue v = condition.eval(scope);
		if(v instanceof SBoolean) {
			SBoolean b = (SBoolean)v;
			return b.getBoolean();
		} else {
			throw new ScriptExecutionException("Condition does not return a boolean", source);
		}
	}

}
