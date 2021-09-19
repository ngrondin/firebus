package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Block;
import io.firebus.script.units.Statement;
import io.firebus.script.values.abs.SValue;

public class Finally extends Statement {
	protected Block block;
	
	public Finally(Block b, SourceInfo uc) {
		super(uc);
		block = b;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		return block.eval(scope);
	}

}
