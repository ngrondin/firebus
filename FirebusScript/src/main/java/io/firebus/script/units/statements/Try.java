package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Statement;
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

	public SValue eval(Scope scope) throws ScriptExecutionException {
		try {
			return block.eval(scope);
		} catch(Exception e) {
			if(catchProd != null) {
				Scope local = new Scope(scope);
				local.declareValue(catchProd.getKey(), new SException(e));
				return catchProd.eval(local);
			} else {
				return SNull.get();
			}
		} finally {
			if(finallyProd != null)
				finallyProd.eval(scope);
		}
	}

}
