package io.firebus.script.units.statements;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Statement;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SValue;
import io.firebus.script.values.flow.SReturn;

public class While extends Statement {
	protected Expression condition;
	protected ExecutionUnit unit;
	
	public While(Expression c, ExecutionUnit u, UnitContext uc) {
		super(uc);
		condition = c;
		unit = u;
	}

	public SValue eval(Scope scope) throws ScriptException {
		while(conditionIsTrue(scope)) {
			SValue ret = unit.eval(scope);
			if(ret instanceof SReturn) {
				return ret;
			}
		}
		return new SNull();
	}
	
	protected boolean conditionIsTrue(Scope scope) throws ScriptException {
		SValue v = condition.eval(scope);
		if(v instanceof SBoolean) {
			SBoolean b = (SBoolean)v;
			return b.getBoolean();
		} else {
			throw new ScriptException("Condition does not return a boolean", context);
		}
	}

}
