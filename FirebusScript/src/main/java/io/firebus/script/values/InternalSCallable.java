package io.firebus.script.values;

import java.util.List;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Block;
import io.firebus.script.values.flow.SReturn;

public class InternalSCallable extends SCallable {
	protected List<String> paramNames;
	protected Block body;
	protected Scope definitionScope;

	public InternalSCallable(List<String> p, Block b, Scope s) {
		paramNames = p;
		body = b;
		definitionScope = s;
	}

	public SValue call(SValue[] arguments) throws ScriptException {
		Scope runScope = new Scope(definitionScope);
		for(int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			SValue so = arguments != null && arguments.length > i ? arguments[i] : null;
			runScope.setValue(paramName, so);			
		}
		SValue ret = body.eval(runScope);
		if(ret instanceof SReturn) {
			return ((SReturn)ret).getReturnedValue();
		} else {
			return new SNull();
		}
	}

}
