package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.OneBooleanOperator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SValue;

public class Not extends OneBooleanOperator {

	public Not(Expression e, UnitContext uc) {
		super(e, uc);
	}

	protected SValue evalWithBoolean(boolean b) throws ScriptException {
		return new SBoolean(!b);
	}


}
