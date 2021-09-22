package io.firebus.script.units.statements;

import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;

public class Switch extends Statement {
	protected Expression expression;
	protected List<CaseClause> caseClauseList;
	
	public Switch(Expression e, List<CaseClause> ccl, SourceInfo uc) {
		super(uc);
		expression = e;
		caseClauseList = ccl;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue value = expression.eval(scope);
		for(CaseClause caseClause: caseClauseList) {
			if(caseClause.expressionMatches(value, scope)) {
				return caseClause.eval(scope);
			}
		}
		return SNull.get();
	}

}
