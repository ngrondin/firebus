package io.firebus.script.units;

import java.util.List;

import io.firebus.script.objects.ScriptObject;
import io.firebus.script.scopes.Scope;

public class Block extends ExecutionUnit {
	protected List<ExecutionUnit> units;
	
	public Block(List<ExecutionUnit> u) {
		units = u;
	}
	
	public ScriptObject eval(Scope scope) {
		ScriptObject ret = null;
		for(ExecutionUnit unit : units) {
			ret = unit.eval(scope);
		}
		return ret;
	}

}
