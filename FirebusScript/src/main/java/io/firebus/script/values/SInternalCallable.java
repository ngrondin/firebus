package io.firebus.script.values;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Block;
import io.firebus.script.values.abs.SContextCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SReturn;

public class SInternalCallable extends SContextCallable {
	protected List<String> paramNames;
	protected Block body;
	protected Scope definitionScope;

	public SInternalCallable(List<String> p, Block b, Scope s) {
		paramNames = p;
		body = b;
		definitionScope = s;
	}
	
	public Scope getDefinitionScope() {
		return definitionScope;
	}
	
	public SValue call(SObject thisObject, SValue... arguments) throws ScriptException {
		Scope runScope = new Scope(definitionScope);
		for(int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			SValue so = arguments != null && arguments.length > i ? arguments[i] : null;
			runScope.setValue(paramName, so);			
		}
		if(thisObject != null) {
			runScope.setValue("this", thisObject);
		}
		SValue ret = body.eval(runScope);
		if(ret instanceof SReturn) {
			return ((SReturn)ret).getReturnedValue();
		} else {
			return new SNull();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(String param: paramNames) {
			if(sb.length() > 1)
				sb.append(",");
			sb.append(param);
		}
		sb.append(") {");
		sb.append(body.toString());
		sb.append("}");
		return sb.toString();
	}


}
