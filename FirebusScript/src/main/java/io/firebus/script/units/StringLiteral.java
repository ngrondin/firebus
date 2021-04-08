package io.firebus.script.units;

import io.firebus.script.objects.ScriptObject;
import io.firebus.script.objects.ScriptString;
import io.firebus.script.scopes.Scope;

public class StringLiteral extends Literal {
	protected String str;
	
	public StringLiteral(String s) {
		str = s;
	}
	
	public ScriptObject eval(Scope scope) {
		return new ScriptString(new String(str));
	}

}
