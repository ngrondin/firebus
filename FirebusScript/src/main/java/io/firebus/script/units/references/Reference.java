package io.firebus.script.units.references;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class Reference extends Expression {
	
	public Reference(SourceInfo uc) {
		super(uc);
	}
	
	public abstract void setValue(Scope scope, SValue val) throws ScriptExecutionException;


}
