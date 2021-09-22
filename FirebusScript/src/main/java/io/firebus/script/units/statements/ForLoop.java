package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.setters.DeclareList;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class ForLoop extends Statement {
	protected DeclareList declares;
	protected Expression initial;
	protected Expression condition;
	protected Operator operator;
	protected ExecutionUnit unit;
	
	public ForLoop(DeclareList d, Expression c, Operator o, ExecutionUnit eu, SourceInfo uc) {
		super(uc);
		declares = d;
		condition = c;
		operator = o;
		unit = eu;
	}
	
	public ForLoop(Expression i, Expression c, Operator o, ExecutionUnit eu, SourceInfo uc) {
		super(uc);
		initial = i;
		condition = c;
		operator = o;
		unit = eu;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		Scope localScope = new Scope(scope);
		if(declares != null)
			declares.eval(localScope);
		else if(initial != null)
			initial.eval(localScope);

		try {
			while(condition.eval(localScope).toBoolean() == true) {
				SValue ret = unit.eval(localScope);
				if(ret instanceof SReturn) {
					return ret;
				} else if(ret instanceof SBreak) {
					return new SNull();
				}
				operator.eval(localScope);
			}
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage(), source);
		}
		return new SNull();
	}
}
