package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Statement;
import io.firebus.script.values.SValue;
import io.firebus.script.values.flow.SBreak;

public class Break extends Statement {

	public Break(SourceInfo uc) {
		super(uc);
	}

	public SValue eval(Scope scope) throws ScriptException {
		return new SBreak();
	}

}
