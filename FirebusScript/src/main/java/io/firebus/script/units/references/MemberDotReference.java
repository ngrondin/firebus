package io.firebus.script.units.references;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.values.DynamicSObject;
import io.firebus.script.values.SObject;
import io.firebus.script.values.SValue;

public class MemberDotReference extends Reference {
	protected Expression objectExpr;
	protected String key;
	
	public MemberDotReference(Expression oe, String k, SourceInfo uc) {
		super(uc);
		objectExpr = oe;
		key = k;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue o = objectExpr.eval(scope);
		if(o instanceof SObject) {
			SObject obj = (SObject)o;
			SValue ret = obj.getMember(key);
			return ret;
		} else {
			throw new ScriptException("Base of a dot reference must be an object", source);
		}
	}

	public void setValue(Scope scope, SValue val) throws ScriptException {
		SValue o = objectExpr.eval(scope);
		if(o instanceof DynamicSObject) {
			DynamicSObject obj = (DynamicSObject)o;
			obj.putMember(key, val);
		} else {
			throw new ScriptException("Base of a dot reference must be a dynamic object", source);
		}
	}

}