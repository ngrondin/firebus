package io.firebus.script.values;

import java.util.List;

import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Block;

public class InternalSCallable extends SCallable {
	protected List<String> paramNames;
	protected Block body;
	protected Scope definitionScope;

	public InternalSCallable(List<String> p, Block b, Scope s) {
		paramNames = p;
		body = b;
		definitionScope = s;
	}

	public SValue call(List<SValue> params) {
		Scope runScope = new Scope(definitionScope);
		for(int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			SValue so = params.size() > i ? params.get(i) : null;
			runScope.setValue(paramName, so);			
		}
		return body.eval(runScope);
	}
}
