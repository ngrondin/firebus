package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SValue;

public abstract class ValueOperator extends Operator {

	public ValueOperator(SourceInfo uc) {
		super(uc);
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		try {
			return valueOpEval(scope);
		} catch(ScriptValueException e) {
			throw new ScriptExecutionException(e.getMessage() + " in '" + this.toString() + "'", source);
		}
	}
	
	protected abstract SValue valueOpEval(Scope scope) throws ScriptExecutionException, ScriptValueException;
}
