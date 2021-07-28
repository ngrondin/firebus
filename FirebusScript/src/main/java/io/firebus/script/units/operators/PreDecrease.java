package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.units.operators.abs.NumberReferenceOperator;
import io.firebus.script.units.references.Reference;

public class PreDecrease extends NumberReferenceOperator {
	public PreDecrease(Reference r, SourceInfo uc) {
		super(r, uc);
	}

	protected Number getUpdateNumber(Number originalNumber) throws ScriptException {
		if(originalNumber instanceof Integer) {
			return originalNumber.intValue() - 1;
		} else {
			return originalNumber.doubleValue() - 1;
		}
	}

	protected Number getReturnNumber(Number originalNumber, Number updatedNumber) throws ScriptException {
		return updatedNumber;
	}
}
