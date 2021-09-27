package io.firebus.script.units.literals;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Literal;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;

public class StringLiteral extends Literal {
	protected String str;
	protected SString sStr;
	
	public StringLiteral(String s, SourceInfo uc) {
		super(uc);
		str = s;
		sStr = new SString(str);
	}
	
	public String getString() {
		return str;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		return sStr;
	}

}
