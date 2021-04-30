package io.firebus.script.units;

import java.util.List;

import io.firebus.script.scopes.Scope;
import io.firebus.script.values.SValue;

public class DeclareList extends ExecutionUnit {
	protected String modifier;
	protected List<Declare> list;
	
	public DeclareList(String m, List<Declare> l) {
		list = l;
		modifier = m;
		for(Declare d : list) {
			d.setModifier(m);
		}
	}
	
	public SValue eval(Scope scope) {
		for(Declare d : list)
			d.eval(scope);
		return null;
	}

}
