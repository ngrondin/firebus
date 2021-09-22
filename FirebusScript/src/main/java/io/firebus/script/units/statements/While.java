package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class While extends Statement {
	protected Expression condition;
	protected ExecutionUnit unit;
	
	public While(Expression c, ExecutionUnit u, SourceInfo uc) {
		super(uc);
		condition = c;
		unit = u;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		Scope localScope = new Scope(scope);
		try {
			while(condition.eval(localScope).toBoolean() == true) {
				SValue ret = unit.eval(localScope);
				if(ret instanceof SReturn) {
					return ret;
				} else if(ret instanceof SBreak) {
					return SNull.get();
				}
			}
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage(), source);
		}
		return SNull.get();
	}
}
