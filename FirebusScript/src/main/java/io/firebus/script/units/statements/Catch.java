package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Block;
import io.firebus.script.units.Statement;
import io.firebus.script.values.abs.SValue;

public class Catch extends Statement {
	protected String name;
	protected Block block;

	public Catch(String n, Block b, SourceInfo uc) {
		super(uc);
		name = n;
		block = b;
	}

	public String getName() {
		return name;
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		return block.eval(scope);
	}

}
