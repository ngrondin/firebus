package io.firebus.script.units.references;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Expression;
import io.firebus.script.values.DynamicSObject;
import io.firebus.script.values.SObject;
import io.firebus.script.values.SValue;

public class MemberDotReference extends KeyBasedReference {
	protected Expression objectExpr;
	
	public MemberDotReference(Expression e, String k, SourceInfo uc) {
		super(k, uc);
		objectExpr = e;
	}
	
	public SObject getObject(Scope scope) throws ScriptException {
		SValue o = objectExpr.eval(scope);
		if(o instanceof SObject) {
			SObject obj = (SObject)o;
			return obj;
		} else {
			throw new ScriptException("Not an object", source);
		}
	}

	public SValue eval(Scope scope) throws ScriptException {
		SObject o = getObject(scope);
		SValue val = o.getMember(key);
		return val;
	}

	public void setValue(Scope scope, SValue val) throws ScriptException {
		SObject o = getObject(scope);
		if(o instanceof DynamicSObject) {
			DynamicSObject obj = (DynamicSObject)o;
			obj.putMember(getKey(), val);
		} else {
			throw new ScriptException("Members can only be set on dynamic objects", source);
		}
	}

}
