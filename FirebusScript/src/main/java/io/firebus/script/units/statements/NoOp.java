package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Statement;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;

public class NoOp extends Statement {

	public NoOp(SourceInfo uc) {
		super(uc);
	}

	public SValue eval(Scope scope) throws ScriptException {
		return new SNull();
	}

}
