package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Block;
import io.firebus.script.units.Statement;
import io.firebus.script.values.SException;
import io.firebus.script.values.abs.SValue;

public class Try extends Statement {
	protected Block block;
	protected Catch catchProd;
	
	public Try(Block b, Catch c, SourceInfo uc) {
		super(uc);
		block = b;
		catchProd = c;
	}

	public SValue eval(Scope scope) throws ScriptException {
		try {
			return block.eval(scope);
		} catch(Exception e) {
			Scope local = new Scope(scope);
			local.setValue(catchProd.getName(), new SException(e));
			return catchProd.eval(local);
		}
	}

}
