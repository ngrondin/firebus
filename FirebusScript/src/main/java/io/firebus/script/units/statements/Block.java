package io.firebus.script.units.statements;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.units.setters.Declare;
import io.firebus.script.units.setters.DeclareList;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class Block extends Statement {
	protected Statement[] declareUnits;
	protected Statement[] executionUnits;
	
	public Block(List<Statement> u, SourceInfo uc) {
		super(uc);
		List<Statement> du = new ArrayList<Statement>();
		List<Statement> eu = new ArrayList<Statement>();
		for(Statement s: u) {
			if(s instanceof Declare || s instanceof DeclareList) 
				du.add(s);
			else
				eu.add(s);
		}
		declareUnits = du.toArray(new Statement[0]);
		executionUnits = eu.toArray(new Statement[0]);
	}
	
	public int getStatementCount() {
		return executionUnits.length;
	}
	
	public Statement getStatement(int i) {
		return executionUnits[i];
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		for(int i = 0; i < declareUnits.length; i++)
			declareUnits[i].eval(scope);
		
		for(int i = 0; i < executionUnits.length; i++) {
			SValue ret = executionUnits[i].eval(scope);
			if(ret instanceof SReturn) {
				return ret;
			} else if(ret instanceof SBreak) {
				return ret;
			}
		}
		return SNull.get();
	}

}
