package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public abstract class NumberReferenceOperator extends ReferenceOperator {

	public NumberReferenceOperator(Reference r, SourceInfo uc) {
		super(r, uc);
	}

	protected SValue getUpdateValue(SValue originalValue) throws ScriptException {
		if(originalValue instanceof SNumber) {
			Number originalNumber = ((SNumber)originalValue).getNumber();
			Number updateNumber = getUpdateNumber(originalNumber);
			return new SNumber(updateNumber);
		} else {
			throw new ScriptException(this.getClass().getSimpleName() + " operator requires a reference to a number", source);
		}
	}

	protected SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptException {
		if(originalValue instanceof SNumber && updatedValue instanceof SNumber) {
			return new SNumber(getReturnNumber(((SNumber)originalValue).getNumber(), ((SNumber)updatedValue).getNumber()));
		} else {
			throw new ScriptException(this.getClass().getSimpleName() + " operator requires a reference to a number", source);
		}
	}

	protected abstract Number getUpdateNumber(Number originalNumber) throws ScriptException;
	
	protected abstract Number getReturnNumber(Number originalNumber, Number updatedNumber) throws ScriptException;
}
