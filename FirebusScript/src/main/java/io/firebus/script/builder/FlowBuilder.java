package io.firebus.script.builder;

import java.util.List;

import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptParser.ExpressionSequenceContext;
import io.firebus.script.parser.JavaScriptParser.ReturnStatementContext;
import io.firebus.script.units.Expression;
import io.firebus.script.units.statements.Return;

public class FlowBuilder extends Builder {

	public static Return buildReturnStatement(ReturnStatementContext ctx) throws ScriptBuildException {
		List<Expression> retExprs = null;
		if(ctx.getChildCount() == 3) {
			retExprs = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(1));
		}
		return new Return(retExprs != null && retExprs.size() > 0 ? retExprs.get(0) : null, sourceInfo(ctx));
	}
}
