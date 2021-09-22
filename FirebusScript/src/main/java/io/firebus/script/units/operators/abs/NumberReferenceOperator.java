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

	protected SValue getUpdateValue(SValue originalValue) throws ScriptExecutionException, ScriptValueException {
		Number originalNumber = originalValue.toNumber();
		Number updateNumber = getUpdateNumber(originalNumber);
		return new SNumber(updateNumber);
	}

	protected abstract Number getUpdateNumber(Number originalNumber) throws ScriptExecutionException, ScriptValueException;
	
}
