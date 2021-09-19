package io.firebus.script.units;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class Block extends Statement {
	protected List<Statement> declareUnits;
	protected List<Statement> executionUnits;
	
	public Block(List<Statement> u, SourceInfo uc) {
		super(uc);
		declareUnits = new ArrayList<Statement>();
		executionUnits = new ArrayList<Statement>();
		for(Statement s: u) {
			if(s instanceof Declare || s instanceof DeclareList) 
				declareUnits.add(s);
			else
				executionUnits.add(s);
		}
	}
	
	public int getStatementCount() {
		return executionUnits.size();
	}
	
	public Statement getStatement(int i) {
		return executionUnits.get(i);
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		for(ExecutionUnit unit : declareUnits)
			unit.eval(scope);
		
		for(ExecutionUnit unit : executionUnits) {
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
