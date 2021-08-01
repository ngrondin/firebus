package io.firebus.script.units.literals;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Literal;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SValue;

public class BooleanLiteral extends Literal {
    protected boolean value;

    public BooleanLiteral(boolean v, SourceInfo uc) {
    	super(uc);
    	value = v;
    }

    public SValue eval(Scope scope) throws ScriptException {
        return new SBoolean(value);
    }
    
}
