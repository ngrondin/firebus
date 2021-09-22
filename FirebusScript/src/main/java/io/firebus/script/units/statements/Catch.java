package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.VariableId;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.values.abs.SValue;

public class Catch extends Statement {
	protected VariableId key;
	protected int nameHashCode;
	protected Block block;

	public Catch(String n, Block b, SourceInfo uc) {
		super(uc);
		key = new VariableId(n);
		block = b;
	}

	public VariableId getKey() {
		return key;
	}
	
	public int getNameHashCode() {
		return nameHashCode;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		return block.eval(scope);
	}

}
