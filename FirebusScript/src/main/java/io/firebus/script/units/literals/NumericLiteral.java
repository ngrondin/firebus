package io.firebus.script.units.literals;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Literal;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;

public class NumericLiteral extends Literal {
    protected Number number;
    protected SNumber sNumber;

    public NumericLiteral(Number n, SourceInfo uc) {
    	super(uc);
        number = n;
        sNumber = new SNumber(number);
    }

    public SValue eval(Scope scope) throws ScriptExecutionException {
        return sNumber;
    }
    
}
