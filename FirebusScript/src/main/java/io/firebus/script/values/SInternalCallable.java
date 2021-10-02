package io.firebus.script.values;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.VariableId;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.statements.Block;
import io.firebus.script.values.abs.SContextCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SReturn;

public class SInternalCallable extends SContextCallable {
	protected VariableId[] paramIds;
	protected Block body;
	protected Scope definitionScope;

	public SInternalCallable(List<String> p, Block b, Scope s) {
		body = b;
		definitionScope = s;
		paramIds = new VariableId[p.size()];
		for(int i = 0; i < p.size(); i++)
			paramIds[i] = new VariableId(p.get(i));
	}
	
	public Scope getDefinitionScope() {
		return definitionScope;
	}

	public String[] getParameters() {
		String[] ret = new String[paramIds.length];
		for(int i = 0; i < ret.length; i++)
			ret[i] = paramIds[i].name;
		return ret;
	}
	
	public Block getBody() {
		return body;
	}
	
	public SValue call(SObject thisObject, SValue... arguments) throws ScriptCallException {
		Scope runScope = new Scope(definitionScope);
		for(int i = 0; i < paramIds.length; i++) {
			SValue so = arguments != null && arguments.length > i ? arguments[i] : null;
			runScope.setValue(paramIds[i], so);			
		}
		if(thisObject != null) {
			runScope.setValue(new VariableId("this"), thisObject);
		}
		try {
			SValue ret = body.eval(runScope);
			if(ret instanceof SReturn) {
				return ((SReturn)ret).getReturnedValue();
			} else {
				return SNull.get();
			}
		} catch(ScriptExecutionException e) {
			throw new ScriptCallException("Error on call", e);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(VariableId param: paramIds) {
			if(sb.length() > 1)
				sb.append(",");
			sb.append(param.name);
		}
		sb.append(") {");
		sb.append(body.toString());
		sb.append("}");
		return sb.toString();
	}


}
