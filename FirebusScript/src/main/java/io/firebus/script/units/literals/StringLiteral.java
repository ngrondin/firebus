package io.firebus.script.units.literals;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Literal;
import io.firebus.script.values.SValue;
import io.firebus.script.values.SString;

public class StringLiteral extends Literal {
	protected String str;
	
	public StringLiteral(String s, SourceInfo uc) {
		super(uc);
		str = s;
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		return new SString(str);
	}

}
