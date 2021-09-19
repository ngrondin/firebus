package io.firebus.script.units;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.values.abs.SValue;

public class DeclareList extends Statement {
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
	
	public List<Declare> getDeclares() {
		return list;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		for(Declare d : list)
			d.eval(scope);
		return null;
	}

}
