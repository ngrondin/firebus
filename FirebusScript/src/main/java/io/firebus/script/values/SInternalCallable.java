package io.firebus.script.values;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.tools.Parameter;
import io.firebus.script.units.statements.Block;
import io.firebus.script.values.abs.SContextCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SReturn;

public class SInternalCallable extends SContextCallable {
	protected String name;
	protected Parameter[] params;
	protected Block body;
	protected Scope definitionScope;

	public SInternalCallable(String n, List<Parameter> p, Block b, Scope s) {
		name = n;
		body = b;
		definitionScope = s;
		params = new Parameter[p.size()];
		for(int i = 0; i < p.size(); i++)
			params[i] = p.get(i);
	}
	
	public Scope getDefinitionScope() {
		return definitionScope;
	}

	public Parameter[] getParameters() {
		return params;
	}
	
	public String[] getParameterNames() {
		String[] ret = new String[params.length];
		for(int i = 0; i < params.length; i++) 
			ret[i] = params[i].name;
		return ret;
	}
	
	public Block getBody() {
		return body;
	}
	
	public SValue call(SObject thisObject, SValue... arguments) throws ScriptCallException {
		Scope runScope = new Scope(definitionScope);
		try {
			for(int i = 0; i < params.length; i++) {
				SValue so = arguments != null && arguments.length > i ? arguments[i] : SNull.get();
				if(so instanceof SNull && params[i].defaultLiteral != null)
					so = params[i].defaultLiteral.eval(definitionScope);
				runScope.declareValue(params[i].name, so);			
			}
			if(thisObject != null) {
				runScope.declareValue("this", thisObject);
			}
			SValue ret = body.eval(runScope);
			if(ret instanceof SReturn) {
				return ((SReturn)ret).getReturnedValue();
			} else {
				return SNull.get();
			}
		} catch(ScriptExecutionException e) {
			throw new ScriptCallException("Error calling " + (name != null ? name : "anonymous"), name, e);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(Parameter param: params) {
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
