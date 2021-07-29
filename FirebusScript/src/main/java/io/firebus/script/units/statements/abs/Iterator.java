package io.firebus.script.units.statements.abs;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Statement;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SValue;
import io.firebus.script.values.flow.SReturn;

public abstract class Iterator extends Statement {
	protected ExecutionUnit unit;

	public Iterator(ExecutionUnit eu, SourceInfo uc) {
		super(uc);
		unit = eu;
	}

	public SValue eval(Scope scope) throws ScriptException {
		Scope localScope = new Scope(scope);
		before(localScope);
		while(continueLoop(localScope)) {
			SValue ret = unit.eval(localScope);
			if(ret instanceof SReturn) {
				return ret;
			}
			afterIteration(localScope);
		}
		return new SNull();
	}
	
	protected abstract void before(Scope scope) throws ScriptException;
	
	protected abstract boolean continueLoop(Scope scope) throws ScriptException;
	
	protected abstract void afterIteration(Scope scope) throws ScriptException;

}
