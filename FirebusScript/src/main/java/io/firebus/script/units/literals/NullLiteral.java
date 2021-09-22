package io.firebus.script.units.literals;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Literal;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;

public class NullLiteral extends Literal {

    public NullLiteral(SourceInfo uc) {
    	super(uc);
    }

    public SValue eval(Scope scope) throws ScriptExecutionException {
        return SNull.get();
    }
    
}
