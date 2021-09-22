package io.firebus.script.units.operators.abs;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.values.abs.SValue;

public abstract class Operator extends Expression {

	public Operator(SourceInfo uc) {
		super(uc);
	}
}
