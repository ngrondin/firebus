package io.firebus.script.units;

import java.util.List;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class Block extends ExecutionUnit {
	protected List<ExecutionUnit> units;
	
	public Block(List<ExecutionUnit> u) {
		units = u;
	}
	
	public SValue eval(Scope scope) {
		SValue ret = null;
		for(ExecutionUnit unit : units) {
			ret = unit.eval(scope);
		}
		return ret;
	}

}
