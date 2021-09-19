package io.firebus.script.units.operators.abs;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;

public abstract class NumberReferenceOperator extends ReferenceOperator {

	public NumberReferenceOperator(Reference r, SourceInfo uc) {
		super(r, uc);
	}

	protected SValue getUpdateValue(SValue originalValue) throws ScriptExecutionException {
		try {
			Number originalNumber = originalValue.toNumber();
			Number updateNumber = getUpdateNumber(originalNumber);
			return new SNumber(updateNumber);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(this.getClass().getSimpleName() + " operator requires a reference to a number", source);			
		}
	}

	protected SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptExecutionException {
		if(originalValue instanceof SNumber && updatedValue instanceof SNumber) {
			return new SNumber(getReturnNumber(((SNumber)originalValue).getNumber(), ((SNumber)updatedValue).getNumber()));
		} else {
			throw new ScriptExecutionException(this.getClass().getSimpleName() + " operator requires a reference to a number", source);
		}
	}

	protected abstract Number getUpdateNumber(Number originalNumber) throws ScriptExecutionException;
	
	protected abstract Number getReturnNumber(Number originalNumber, Number updatedNumber) throws ScriptExecutionException;
}
