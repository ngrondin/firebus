package io.firebus.script.units.setters;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.statements.Block;
import io.firebus.script.values.SInternalCallable;
import io.firebus.script.values.abs.SValue;

public class CallableDefinition extends Expression {
	protected List<String> params;
	protected Block body;
	
	public CallableDefinition(List<String> p, Block b, SourceInfo uc) {
		super(uc);
		params = p;
		body = b;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		Scope local = new Scope(scope);
		return new SInternalCallable(params, body, local);
	}

}
