package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Block;
import io.firebus.script.units.Statement;
import io.firebus.script.values.SException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;

public class Try extends Statement {
	protected Block block;
	protected Catch catchProd;
	protected Finally finallyProd;
	
	public Try(Block b, Catch c, Finally f, SourceInfo uc) {
		super(uc);
		block = b;
		catchProd = c;
		finallyProd = f;
	}

	public SValue eval(Scope scope) throws ScriptException {
		try {
			return block.eval(scope);
		} catch(Exception e) {
			if(catchProd != null) {
				Scope local = new Scope(scope);
				local.setValue(catchProd.getName(), new SException(e));
				return catchProd.eval(local);
			} else {
				return new SNull();
			}
		} finally {
			if(finallyProd != null)
				finallyProd.eval(scope);
		}
	}

}
