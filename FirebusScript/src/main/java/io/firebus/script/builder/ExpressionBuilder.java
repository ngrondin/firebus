package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.parser.JavaScriptParser.AdditiveExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ArgumentsExpressionContext;
import io.firebus.script.parser.JavaScriptParser.AssignmentExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitAndExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitNotExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitOrExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitShiftExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitXOrExpressionContext;
import io.firebus.script.parser.JavaScriptParser.CoalesceExpressionContext;
import io.firebus.script.parser.JavaScriptParser.EqualityExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ExpressionSequenceContext;
import io.firebus.script.parser.JavaScriptParser.ExpressionStatementContext;
import io.firebus.script.parser.JavaScriptParser.FunctionExpressionContext;
import io.firebus.script.parser.JavaScriptParser.IdentifierContext;
import io.firebus.script.parser.JavaScriptParser.IdentifierExpressionContext;
import io.firebus.script.parser.JavaScriptParser.LiteralContext;
import io.firebus.script.parser.JavaScriptParser.LiteralExpressionContext;
import io.firebus.script.parser.JavaScriptParser.LogicalAndExpressionContext;
import io.firebus.script.parser.JavaScriptParser.LogicalOrExpressionContext;
import io.firebus.script.parser.JavaScriptParser.MultiplicativeExpressionContext;
import io.firebus.script.parser.JavaScriptParser.NotExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PostDecreaseExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PostIncrementExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PowerExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PreDecreaseExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PreIncrementExpressionContext;
import io.firebus.script.parser.JavaScriptParser.RelationalExpressionContext;
import io.firebus.script.parser.JavaScriptParser.SingleExpressionContext;
import io.firebus.script.parser.JavaScriptParser.UnaryMinusExpressionContext;
import io.firebus.script.parser.JavaScriptParser.UnaryPlusExpressionContext;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Reference;
import io.firebus.script.units.Setter;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.Add;
import io.firebus.script.units.operators.BitAnd;
import io.firebus.script.units.operators.BitNot;
import io.firebus.script.units.operators.BitOr;
import io.firebus.script.units.operators.BitShiftLeft;
import io.firebus.script.units.operators.BitShiftRight;
import io.firebus.script.units.operators.BitShiftRightLogical;
import io.firebus.script.units.operators.BitXOr;
import io.firebus.script.units.operators.Coalesce;
import io.firebus.script.units.operators.Decrease;
import io.firebus.script.units.operators.Divide;
import io.firebus.script.units.operators.EqualityCompare;
import io.firebus.script.units.operators.Increase;
import io.firebus.script.units.operators.LogicalAnd;
import io.firebus.script.units.operators.LogicalOr;
import io.firebus.script.units.operators.Modulus;
import io.firebus.script.units.operators.Multiply;
import io.firebus.script.units.operators.Not;
import io.firebus.script.units.operators.Power;
import io.firebus.script.units.operators.PreDecrease;
import io.firebus.script.units.operators.PreIncrease;
import io.firebus.script.units.operators.RelationalCompare;
import io.firebus.script.units.operators.Substract;
import io.firebus.script.units.operators.UnaryMinus;
import io.firebus.script.units.operators.UnaryPlus;

public class ExpressionBuilder {
	    
	public static Expression buildExpressionStatement(ExpressionStatementContext ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof ExpressionSequenceContext) {
			List<Expression> list = buildExpressionSequence((ExpressionSequenceContext)sub);
			if(list.size() > 0) 
				return list.get(0);
			else
				return null;
		} else {
			return null;
		}
	}
	
	public static List<Expression> buildExpressionSequence(ExpressionSequenceContext ctx) {
		List<Expression> ret = new ArrayList<Expression>();
		for(ParseTree sub: ctx.children) {
			Expression expr = null;
			if(sub instanceof SingleExpressionContext) {
				expr = buildSingleExpression((SingleExpressionContext)sub);
			} 
			ret.add(expr);
		}
		return ret;
	}
	

	
	public static Expression buildSingleExpression(SingleExpressionContext ctx) {
		UnitContext uc = ContextBuilder.buildContext(ctx);
		if(ctx instanceof AssignmentExpressionContext) {
			return new Setter(ctx.getChild(0).getText(), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof ArgumentsExpressionContext) {
			return CallableBuilder.buildArgumentsExpression((ArgumentsExpressionContext)ctx);	
		} else if(ctx instanceof FunctionExpressionContext) {
			return CallableBuilder.buildFunctionExpression((FunctionExpressionContext)ctx);
		} else if(ctx instanceof IdentifierExpressionContext) {
			return ReferenceBuilder.buildIdentifier((IdentifierContext)ctx.getChild(0));		
		} else if(ctx instanceof LiteralExpressionContext) {
			return LiteralBuilder.buildLiteral((LiteralContext)ctx.getChild(0));
		} else if(ctx instanceof PostIncrementExpressionContext) {
			return new Increase((Reference)buildSingleExpressionFromChild(ctx, 0), uc);
		} else if(ctx instanceof PostDecreaseExpressionContext) {
			return new Decrease((Reference)buildSingleExpressionFromChild(ctx, 0), uc);
		} else if(ctx instanceof PreIncrementExpressionContext) {
			return new PreIncrease((Reference)buildSingleExpressionFromChild(ctx, 1), uc);
		} else if(ctx instanceof PreDecreaseExpressionContext) {
			return new PreDecrease((Reference)buildSingleExpressionFromChild(ctx, 1), uc);
		} else if(ctx instanceof UnaryPlusExpressionContext) {
			return new UnaryPlus(buildSingleExpressionFromChild(ctx, 0), uc);
		} else if(ctx instanceof UnaryMinusExpressionContext) {
			return new UnaryMinus(buildSingleExpressionFromChild(ctx, 0), uc);
		} else if(ctx instanceof BitNotExpressionContext) {
			return new BitNot(buildSingleExpressionFromChild(ctx, 0), uc);
		} else if(ctx instanceof BitAndExpressionContext) {
			return new BitAnd(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);	
		} else if(ctx instanceof BitXOrExpressionContext) {
			return new BitXOr(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);	
		} else if(ctx instanceof BitOrExpressionContext) {
			return new BitOr(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);	
		} else if(ctx instanceof BitShiftExpressionContext) {
			TerminalNode op = (TerminalNode)ctx.getChild(1);
			if(op.getSymbol().getType() == JavaScriptParser.LeftShiftArithmetic)
				return new BitShiftLeft(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);	
			else if(op.getSymbol().getType() == JavaScriptParser.RightShiftArithmetic)
				return new BitShiftRight(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);	
			else if(op.getSymbol().getType() == JavaScriptParser.RightShiftLogical)
				return new BitShiftRightLogical(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else 
				return null;
		} else if(ctx instanceof LogicalAndExpressionContext) {
			return new LogicalAnd(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof LogicalOrExpressionContext) {
			return new LogicalOr(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof NotExpressionContext) {
			return new Not(buildSingleExpressionFromChild(ctx, 1), uc);
		} else if(ctx instanceof MultiplicativeExpressionContext) {
			TerminalNode op = (TerminalNode)ctx.getChild(1);
			if(op.getSymbol().getType() == JavaScriptParser.Multiply)
				return new Multiply(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.getSymbol().getType() == JavaScriptParser.Divide)
				return new Divide(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.getSymbol().getType() == JavaScriptParser.Modulus)
				return new Modulus(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else
				return null;
		} else if(ctx instanceof AdditiveExpressionContext) {
			TerminalNode op = (TerminalNode)ctx.getChild(1);
			if(op.getSymbol().getType() == JavaScriptParser.Plus)
				return new Add(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.getSymbol().getType() == JavaScriptParser.Minus)
				return new Substract(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else
				return null;
		} else if(ctx instanceof PowerExpressionContext) {
			return new Power(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof CoalesceExpressionContext) {
			return new Coalesce(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof RelationalExpressionContext) {
			return new RelationalCompare(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), ctx.getChild(1).getText(), uc);
		} else if(ctx instanceof EqualityExpressionContext) {
			return new EqualityCompare(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), ctx.getChild(1).getText(), uc);
		} else {
			return null;
		}
	}
	
	public static Expression buildSingleExpressionFromChild(ParseTree parentContext, int childIndex) {
		return buildSingleExpression((SingleExpressionContext)parentContext.getChild(childIndex));
	}
	

}
