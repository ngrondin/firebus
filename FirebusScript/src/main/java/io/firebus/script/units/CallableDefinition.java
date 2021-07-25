package io.firebus.script.units;

import java.util.List;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.InternalSCallable;
import io.firebus.script.values.SValue;

public class CallableDefinition extends Expression {
	protected List<String> params;
	protected Block body;
	
	public CallableDefinition(List<String> p, Block b, UnitContext uc) {
		super(uc);
		params = p;
		body = b;
	}
	
	public SValue eval(Scope scope) {
		return new InternalSCallable(params, body, scope);
	}

}
