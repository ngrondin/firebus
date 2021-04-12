package io.firebus.script.objects;

import java.util.List;

import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Block;

public class InternalCallable extends Callable {
	protected List<String> paramNames;
	protected Block body;
	protected Scope definitionScope;

	public InternalCallable(List<String> p, Block b, Scope s) {
		paramNames = p;
		body = b;
		definitionScope = s;
	}

	public ScriptObject call(List<ScriptObject> params) {
		Scope runScope = new Scope(definitionScope);
		for(int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			ScriptObject so = params.size() > i ? params.get(i) : null;
			runScope.setValue(paramName, so);			
		}
		return body.eval(runScope);
	}
}
