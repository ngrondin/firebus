package io.firebus.script.units.statements.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SValue;

public abstract class ConditionalIterator extends Iterator {
	protected Expression condition;
	
	public ConditionalIterator(Expression c, ExecutionUnit u, UnitContext uc) {
		super(u, uc);
		condition = c;
	}

	protected boolean continueLoop(Scope scope) throws ScriptException {
		SValue v = condition.eval(scope);
		if(v instanceof SBoolean) {
			SBoolean b = (SBoolean)v;
			return b.getBoolean();
		} else {
			throw new ScriptException("Condition does not return a boolean", context);
		}
	}

}
