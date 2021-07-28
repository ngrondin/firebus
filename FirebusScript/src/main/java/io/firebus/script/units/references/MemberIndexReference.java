package io.firebus.script.units.references;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Expression;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SValue;

public class MemberIndexReference extends IndexBasedReference {
	protected Expression arrayExpr;
	
	public MemberIndexReference(Expression e, int i, SourceInfo uc) {
		super(i, uc);
		arrayExpr = e;
	}
	
	public SArray getArray(Scope scope) throws ScriptException {
		SValue a = arrayExpr.eval(scope);
		if(a instanceof SArray) {
			return (SArray)a;
		} else {
			throw new ScriptException("Not an array", source);
		}
	}

	public SValue eval(Scope scope) throws ScriptException {
		SArray a = getArray(scope);
		SValue val = a.get(getIndex());
		return val;
	}

	public void setValue(Scope scope, SValue val) throws ScriptException {
		SArray a = getArray(scope);
		a.set(index, val);
	}

}
