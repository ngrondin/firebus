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
import io.firebus.script.values.flow.SSkipExpression;

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

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue v = condition.eval(scope);
		try {
			boolean conditionResult = v instanceof SSkipExpression ? false : v.toBoolean();
			if(conditionResult == true) {
				Scope localScope = new Scope(scope);
				SValue ret = unit.eval(localScope);
				if(ret instanceof SReturn) {
					return ret;
				} else if(ret instanceof SBreak) {
					return ret;
				}
			} else if(elseUnit != null) {
				Scope localScope = new Scope(scope);
				SValue ret = elseUnit.eval(localScope);
				if(ret instanceof SReturn) {
					return ret;
				}					
			}
			return SNull.get();
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException("Condition does not return a boolean", e, source);
		}			
	}
}
