package io.firebus.script.units.statements;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Statement;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SValue;
import io.firebus.script.values.flow.SReturn;

public class If extends Statement {
	protected Expression condition;
	protected ExecutionUnit unit;
	protected ExecutionUnit elseUnit;
	
	public If(Expression c, ExecutionUnit u, ExecutionUnit eu, SourceInfo uc) {
		super(uc);
		condition = c;
		unit = u;
		elseUnit = eu;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue v = condition.eval(scope);
		if(v instanceof SBoolean) {
			SBoolean b = (SBoolean)v;
			if(b.getBoolean() == true) {
				Scope localScope = new Scope(scope);
				SValue ret = unit.eval(localScope);
				if(ret instanceof SReturn) {
					return ret;
				}				
			} else if(elseUnit != null) {
				Scope localScope = new Scope(scope);
				SValue ret = elseUnit.eval(localScope);
				if(ret instanceof SReturn) {
					return ret;
				}					
			}
			return new SNull();
		} else {
			throw new ScriptException("Condition does not return a boolean", source);
		}
	}

}
