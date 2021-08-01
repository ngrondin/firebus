package io.firebus.script.units.references;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.values.DynamicSObject;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SObject;
import io.firebus.script.values.SValue;

public class MemberIndexReference extends Reference {
	protected Expression baseExpr;
	protected Expression indexExpr;
	
	public MemberIndexReference(Expression be, Expression ie, SourceInfo uc) {
		super(uc);
		baseExpr = be;
		indexExpr = ie;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue base = baseExpr.eval(scope);
		if(base instanceof SArray) {
			SArray a = (SArray)base;
			SValue index = indexExpr.eval(scope);
			if(index instanceof SNumber) {
				SValue ret = a.get(((SNumber)index).getNumber().intValue());
				return ret;
			} else {
				throw new ScriptException("Index needs to be an integer on an array", source);
			}
		} else if(base instanceof SObject) {
			SObject o = (SObject)base;
			SValue key = indexExpr.eval(scope);
			SValue ret = o.getMember(key.toString());
			return ret;
		} else {
			throw new ScriptException("Index reference base must be an array or an object", source);
		}
	}

	public void setValue(Scope scope, SValue val) throws ScriptException {
		SValue base = baseExpr.eval(scope);
		if(base instanceof SArray) {
			SArray a = (SArray)base;
			SValue index = indexExpr.eval(scope);
			if(index instanceof SNumber) {
				int i = ((SNumber)index).getNumber().intValue();
				a.set(i, val);
			} else {
				throw new ScriptException("Index needs to be an integer on an array", source);
			}
		} else if(base instanceof DynamicSObject) {
			DynamicSObject o = (DynamicSObject)base;
			SValue key = indexExpr.eval(scope);
			o.putMember(key.toString(), val);
		} else {
			throw new ScriptException("Index reference base must be an array or an object", source);
		}
	}

}