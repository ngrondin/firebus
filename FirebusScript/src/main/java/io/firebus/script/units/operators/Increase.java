package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Reference;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.NumberReferenceOperator;

public class Increase extends NumberReferenceOperator {
	public Increase(Reference r, UnitContext uc) {
		super(r, uc);
	}

	protected Number getUpdateNumber(Number originalNumber) throws ScriptException {
		if(originalNumber instanceof Integer) {
			return originalNumber.intValue() + 1;
		} else {
			return originalNumber.doubleValue() + 1;
		}
	}

	protected Number getReturnNumber(Number originalNumber, Number updatedNumber) throws ScriptException {
		return originalNumber;
	}
}
