package io.firebus.script.units.references;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.values.SMemberCallable;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SDynamicObject;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

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
			if(ret instanceof SCallable) 
				ret = new SMemberCallable(obj, (SCallable)ret);
			else if(ret == null) 
				ret = new SUndefined();
			return ret;
		} else if(o instanceof SUndefined) {
			throw new ScriptException("Cannot get member '" + key + "' of undefined", source);
		} else if(o instanceof SNull) {
			throw new ScriptException("Cannot get member '" + key + "' of null", source);
		} else {
			throw new ScriptException("Cannot get member '" + key + "' of '" + objectExpr.toString() + "' as it is not an object", source);
		}
	}

	public void setValue(Scope scope, SValue val) throws ScriptException {
		SValue o = objectExpr.eval(scope);
		if(o instanceof SDynamicObject) {
			SDynamicObject obj = (SDynamicObject)o;
			obj.putMember(key, val);
		} else {
			throw new ScriptException("Base of a dot reference must be a dynamic object", source);
		}
	}

}
