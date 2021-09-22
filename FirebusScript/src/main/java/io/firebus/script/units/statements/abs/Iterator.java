package io.firebus.script.units.statements.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public abstract class Iterator extends Statement {
	protected ExecutionUnit unit;

	public Iterator(ExecutionUnit eu, SourceInfo uc) {
		super(uc);
		unit = eu;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		Scope localScope = new Scope(scope);
		before(localScope);
		while(continueLoop(localScope)) {
			SValue ret = unit.eval(localScope);
			if(ret instanceof SReturn) {
				return ret;
			} else if(ret instanceof SBreak) {
				return new SNull();
			}
			afterIteration(localScope);
		}
		return new SNull();
	}
	
	protected abstract void before(Scope scope) throws ScriptExecutionException;
	
	protected abstract boolean continueLoop(Scope scope) throws ScriptExecutionException;
	
	protected abstract void afterIteration(Scope scope) throws ScriptExecutionException;

}
