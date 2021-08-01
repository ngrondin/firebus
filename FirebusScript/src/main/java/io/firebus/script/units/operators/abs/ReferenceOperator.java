package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.references.MemberDotReference;
import io.firebus.script.units.references.MemberIndexReference;
import io.firebus.script.units.references.Reference;
import io.firebus.script.units.references.VariableReference;
import io.firebus.script.values.SValue;

public abstract class ReferenceOperator extends Operator {
	protected Reference ref;

	public ReferenceOperator(Reference r, SourceInfo uc) {
		super(uc);
		ref = r;
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		SValue originalValue = ref.eval(scope);
		SValue updateValue = getUpdateValue(originalValue);
		SValue returnValue = getReturnValue(originalValue, updateValue);
		ref.setValue(scope, updateValue);
		/*if(ref instanceof VariableReference) {
			VariableReference vr = (VariableReference)ref;
			Scope targetScope = scope.getScopeOf(vr.getName());
			targetScope.setValue(vr.getName(), updateValue);
		} else if(ref instanceof MemberDotReference) {
			MemberDotReference mdr = (MemberDotReference)ref;
			mdr.set
		} else if(ref instanceof MemberIndexReference) {
			
		}*/
		return returnValue;
	}
	
	protected abstract SValue getUpdateValue(SValue originalValue) throws ScriptException;
	
	protected abstract SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptException;
}
