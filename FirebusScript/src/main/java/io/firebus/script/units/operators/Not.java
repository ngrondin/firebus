package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.OneBooleanOperator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SValue;

public class Not extends OneBooleanOperator {

	public Not(Expression e, SourceInfo uc) {
		super(e, uc);
	}

	protected SValue evalWithBoolean(boolean b) throws ScriptException {
		return new SBoolean(!b);
	}


}
