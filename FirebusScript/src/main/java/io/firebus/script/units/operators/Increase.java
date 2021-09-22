package io.firebus.script.units.operators;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.tools.Operations;
import io.firebus.script.units.operators.abs.ValueOperator;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;

public class Increase extends ValueOperator {
	protected Reference ref;
	
	public Increase(Reference r, SourceInfo uc) {
		super(uc);
		ref = r;
	}

	protected SValue valueOpEval(Scope scope) throws ScriptExecutionException, ScriptValueException {
		SValue originalValue = ref.eval(scope);
		SValue updateValue =  Operations.add(originalValue, new SNumber(1));
		ref.setValue(scope, updateValue);
		return originalValue;
	}
}
