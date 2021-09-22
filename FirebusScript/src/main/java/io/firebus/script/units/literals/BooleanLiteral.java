package io.firebus.script.units.literals;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Literal;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SValue;

public class BooleanLiteral extends Literal {
    protected boolean value;
    protected SBoolean sBoolean;

    public BooleanLiteral(boolean v, SourceInfo uc) {
    	super(uc);
    	value = v;
    	sBoolean = SBoolean.get(v);
    }

    public SValue eval(Scope scope) throws ScriptExecutionException {
        return sBoolean;
    }
    
}
