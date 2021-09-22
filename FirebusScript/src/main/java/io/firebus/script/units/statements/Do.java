package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class Do extends Statement {
	protected Expression condition;
	protected ExecutionUnit unit;
	
	public Do(ExecutionUnit u, Expression c, SourceInfo uc) {
		super(uc);
		condition = c;
		unit = u;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		Scope localScope = new Scope(scope);
		do {
			SValue ret = unit.eval(localScope);
			if(ret instanceof SReturn) {
				return ret;
			} else if(ret instanceof SBreak) {
				return new SNull();
			}
		} while(continueLoop(localScope));
		return new SNull();
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
