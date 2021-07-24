package io.firebus.script.units;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;
import io.firebus.script.values.SString;

public class StringLiteral extends Literal {
	protected String str;
	
	public StringLiteral(String s) {
		str = s;
	}
	
	public SValue eval(Scope scope) {
		return new SString(str);
	}

}
