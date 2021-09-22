package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.operators.abs.NumberReferenceOperator;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.abs.SValue;

public class Decrease extends NumberReferenceOperator {

	public Decrease(Reference r, SourceInfo uc) {
		super(r, uc);
	}

	protected Number getUpdateNumber(Number originalNumber) throws ScriptExecutionException {
		if(originalNumber instanceof Integer) {
			return originalNumber.intValue() - 1;
		} else {
			return originalNumber.doubleValue() - 1;
		}
	}

	protected SValue getReturnValue(SValue originalValue, SValue updatedValue) throws ScriptExecutionException, ScriptValueException {
		return originalValue;
	}


}
