package io.firebus.script.units.statements;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Statement;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class CaseClause extends Statement {
	protected Expression expression;
	protected List<Statement> statementList;
	
	public CaseClause(Expression e, List<Statement> sl, SourceInfo uc) {
		super(uc);
		expression = e;
		statementList = sl;
	}
	
	public boolean expressionMatches(SValue value, Scope scope) throws ScriptException {
		SValue caseValue = expression.eval(scope);
		return caseValue.equals(value);
	}

	public SValue eval(Scope scope) throws ScriptException {
		for(Statement statement: statementList) {
			SValue ret = statement.eval(scope);
			if(ret instanceof SReturn) {
				return ret;
			} else if(ret instanceof SBreak) {
				return ret;
			}
		}
		return null;
	}

}
