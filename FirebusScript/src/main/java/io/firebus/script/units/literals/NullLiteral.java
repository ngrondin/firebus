package io.firebus.script.units.literals;

import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Literal;
import io.firebus.script.units.UnitContext;
import io.firebus.script.ScriptException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SValue;

public class NullLiteral extends Literal {

    public NullLiteral(UnitContext uc) {
    	super(uc);
    }

    public SValue eval(Scope scope) throws ScriptException {
        return new SNull();
    }
    
}
