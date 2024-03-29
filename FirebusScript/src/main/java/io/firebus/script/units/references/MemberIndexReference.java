package io.firebus.script.units.references;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SMemberCallable;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SDynamicObject;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SSkipExpression;

public class MemberIndexReference extends Reference {
	protected Expression baseExpr;
	protected Expression indexExpr;
	
	public MemberIndexReference(Expression be, Expression ie, SourceInfo uc) {
		super(uc);
		baseExpr = be;
		indexExpr = ie;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue base = baseExpr.eval(scope);
		if(base instanceof SArray) {
			SArray a = (SArray)base;
			SValue index = indexExpr.eval(scope);
			if(index instanceof SSkipExpression) {
				return index;
			} else if(index instanceof SNumber) {
				SValue ret = a.get(((SNumber)index).getNumber().intValue());
				if(ret instanceof SCallable) 
					ret = new SMemberCallable(a, (SCallable)ret);
				return ret;
			} else {
				throw new ScriptExecutionException("Index needs to be an integer on an array", source);
			}
		} else if(base instanceof SObject) {
			SObject o = (SObject)base;
			SValue key = indexExpr.eval(scope);
			if(key instanceof SSkipExpression) return key;
			SValue ret = o.getMember(key.toString());
			if(ret instanceof SCallable) 
				ret = new SMemberCallable(o, (SCallable)ret);
			return ret;
		} else if(base instanceof SSkipExpression) {
			return base;
		} else {
			throw new ScriptExecutionException("Index reference base must be an array or an object", source);
		}
	}

	public void setValue(Scope scope, SValue val) throws ScriptExecutionException {
		SValue value = val;
		if(value instanceof SSkipExpression) value = SNull.get();
		SValue base = baseExpr.eval(scope);
		if(base instanceof SArray) {
			SArray a = (SArray)base;
			SValue index = indexExpr.eval(scope);
			if(index instanceof SNumber) {
				int i = ((SNumber)index).getNumber().intValue();
				a.set(i, value);
			} else {
				throw new ScriptExecutionException("Index needs to be an integer on an array", source);
			}
		} else if(base instanceof SDynamicObject) {
			SDynamicObject o = (SDynamicObject)base;
			SValue key = indexExpr.eval(scope);
			try {
				o.putMember(key.toString(), value);
			} catch(ScriptValueException e) {
				throw new ScriptExecutionException("Error setting property of object", e, source);
			}
		} else {
			throw new ScriptExecutionException("Index reference base must be an array or an object", source);
		}
	}

}