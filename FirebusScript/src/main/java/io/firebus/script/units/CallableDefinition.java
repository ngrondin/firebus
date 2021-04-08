package io.firebus.script.units;

import java.util.List;

import io.firebus.script.objects.InternalCallable;
import io.firebus.script.objects.ScriptObject;
import io.firebus.script.scopes.Scope;

public class CallableDefinition extends Expression {
	protected List<String> params;
	protected Block body;
	
	public CallableDefinition(List<String> p, Block b) {
		params = p;
		body = b;
	}
	
	public ScriptObject eval(Scope scope) {
		return new InternalCallable(params, body, scope);
	}

}
