package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.operators.abs.NumberReferenceOperator;
import io.firebus.script.units.references.Reference;

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

	protected Number getReturnNumber(Number originalNumber, Number updatedNumber) throws ScriptExecutionException {
		return originalNumber;
	}

}
