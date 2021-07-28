package io.firebus.script.units;

import java.util.List;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class DeclareList extends ExecutionUnit {
	protected String modifier;
	protected List<Declare> list;
	
	public DeclareList(String m, List<Declare> l, SourceInfo uc) {
		super(uc);
		list = l;
		modifier = m;
		for(Declare d : list) {
			d.setModifier(m);
		}
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		for(Declare d : list)
			d.eval(scope);
		return null;
	}

}
