package io.firebus.script.units.operators;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.references.MemberDotReference;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SDynamicObject;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

public class Delete extends Operator {
	protected Expression expr;

	
	public Delete(Expression e, SourceInfo uc) {
		super(uc);
		expr = e;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		if(expr instanceof MemberDotReference) {
			MemberDotReference mde = (MemberDotReference)expr;
			SObject o = mde.getObject(scope);
			if(o instanceof SDynamicObject) {
				SDynamicObject sdo = (SDynamicObject)o;
				sdo.removeMember(mde.getKey());
			} else {
				throw new ScriptExecutionException("Can only delete a member of a dynamic object", source);
			}
		} else {
			throw new ScriptExecutionException("Delete requires a member dot expression", source);
		}
		return SNull.get();
	}
}
