package io.firebus.script.units.literals;

import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Literal;
import io.firebus.script.units.UnitContext;
import io.firebus.script.ScriptException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public class NumericLiteral extends Literal {
    protected Number number;

    public NumericLiteral(Number n, UnitContext uc) {
    	super(uc);
        number = n;
    }

    public SValue eval(Scope scope) throws ScriptException {
        return new SNumber(number);
    }
    
}