package io.firebus.script.units.setters;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.tools.Parameter;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.statements.Block;
import io.firebus.script.values.SInternalCallable;
import io.firebus.script.values.abs.SValue;

public class CallableDefinition extends Expression {
	protected String name;
	protected List<Parameter> params;
	protected Block body;
	
	public CallableDefinition(String n, List<Parameter> p, Block b, SourceInfo uc) {
		super(uc);
		name = n;
		params = p;
		body = b;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		Scope local = new Scope(scope);
		return new SInternalCallable(name, params, body, local);
	}

}
