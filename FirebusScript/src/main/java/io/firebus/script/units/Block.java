package io.firebus.script.units;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class Block extends ExecutionUnit {
	protected List<ExecutionUnit> units;
	
	public Block(List<ExecutionUnit> u, SourceInfo uc) {
		super(uc);
		units = u;
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		for(ExecutionUnit unit : units) {
			SValue ret = unit.eval(scope);
			if(ret instanceof SReturn) {
				return ret;
			} else if(ret instanceof SBreak) {
				return ret;
			}
		}
		return new SNull();
	}

}
