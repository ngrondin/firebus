package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.parser.JavaScriptParser.AdditiveExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ArgumentsExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ArrayLiteralContext;
import io.firebus.script.parser.JavaScriptParser.ArrayLiteralExpressionContext;
import io.firebus.script.parser.JavaScriptParser.AssignmentExpressionContext;
import io.firebus.script.parser.JavaScriptParser.AssignmentOperatorContext;
import io.firebus.script.parser.JavaScriptParser.AssignmentOperatorExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitAndExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitNotExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitOrExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitShiftExpressionContext;
import io.firebus.script.parser.JavaScriptParser.BitXOrExpressionContext;
import io.firebus.script.parser.JavaScriptParser.CoalesceExpressionContext;
import io.firebus.script.parser.JavaScriptParser.DeleteExpressionContext;
import io.firebus.script.parser.JavaScriptParser.EqualityExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ExpressionSequenceContext;
import io.firebus.script.parser.JavaScriptParser.FunctionExpressionContext;
import io.firebus.script.parser.JavaScriptParser.IdentifierContext;
import io.firebus.script.parser.JavaScriptParser.IdentifierExpressionContext;
import io.firebus.script.parser.JavaScriptParser.InExpressionContext;
import io.firebus.script.parser.JavaScriptParser.LiteralContext;
import io.firebus.script.parser.JavaScriptParser.LiteralExpressionContext;
import io.firebus.script.parser.JavaScriptParser.LogicalAndExpressionContext;
import io.firebus.script.parser.JavaScriptParser.LogicalOrExpressionContext;
import io.firebus.script.parser.JavaScriptParser.MemberDotExpressionContext;
import io.firebus.script.parser.JavaScriptParser.MemberIndexExpressionContext;
import io.firebus.script.parser.JavaScriptParser.MemberOptionalDotExpressionContext;
import io.firebus.script.parser.JavaScriptParser.MultiplicativeExpressionContext;
import io.firebus.script.parser.JavaScriptParser.NewExpressionContext;
import io.firebus.script.parser.JavaScriptParser.NotExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ObjectLiteralContext;
import io.firebus.script.parser.JavaScriptParser.ObjectLiteralExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ParenthesizedExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PostDecreaseExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PostIncrementExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PowerExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PreDecreaseExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PreIncrementExpressionContext;
import io.firebus.script.parser.JavaScriptParser.RelationalExpressionContext;
import io.firebus.script.parser.JavaScriptParser.SingleExpressionContext;
import io.firebus.script.parser.JavaScriptParser.TernaryExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ThisExpressionContext;
import io.firebus.script.parser.JavaScriptParser.TypeofExpressionContext;
import io.firebus.script.parser.JavaScriptParser.UnaryMinusExpressionContext;
import io.firebus.script.parser.JavaScriptParser.UnaryPlusExpressionContext;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.expressions.TernaryExpression;
import io.firebus.script.units.operators.Add;
import io.firebus.script.units.operators.AddSet;
import io.firebus.script.units.operators.BitAnd;
import io.firebus.script.units.operators.BitNot;
import io.firebus.script.units.operators.BitOr;
import io.firebus.script.units.operators.BitShiftLeft;
import io.firebus.script.units.operators.BitShiftRight;
import io.firebus.script.units.operators.BitShiftRightLogical;
import io.firebus.script.units.operators.BitXOr;
import io.firebus.script.units.operators.Coalesce;
import io.firebus.script.units.operators.Decrease;
import io.firebus.script.units.operators.Delete;
import io.firebus.script.units.operators.Divide;
import io.firebus.script.units.operators.DivideSet;
import io.firebus.script.units.operators.Equals;
import io.firebus.script.units.operators.GreaterEqualThan;
import io.firebus.script.units.operators.GreaterThan;
import io.firebus.script.units.operators.Identical;
import io.firebus.script.units.operators.In;
import io.firebus.script.units.operators.Increase;
import io.firebus.script.units.operators.LessEqualThan;
import io.firebus.script.units.operators.LessThan;
import io.firebus.script.units.operators.LogicalAnd;
import io.firebus.script.units.operators.LogicalOr;
import io.firebus.script.units.operators.Modulus;
import io.firebus.script.units.operators.Multiply;
import io.firebus.script.units.operators.MultiplySet;
import io.firebus.script.units.operators.Not;
import io.firebus.script.units.operators.NotEquals;
import io.firebus.script.units.operators.NotIdentical;
import io.firebus.script.units.operators.Power;
import io.firebus.script.units.operators.PreDecrease;
import io.firebus.script.units.operators.PreIncrease;
import io.firebus.script.units.operators.Substract;
import io.firebus.script.units.operators.SubstractSet;
import io.firebus.script.units.operators.Typeof;
import io.firebus.script.units.operators.UnaryMinus;
import io.firebus.script.units.operators.UnaryPlus;
import io.firebus.script.units.references.MemberDotReference;
import io.firebus.script.units.references.MemberIndexReference;
import io.firebus.script.units.references.MemberOptionalDotReference;
import io.firebus.script.units.references.Reference;
import io.firebus.script.units.references.VariableReference;
import io.firebus.script.units.setters.Setter;

public class ExpressionBuilder extends Builder {
	    

	
	public static List<Expression> buildExpressionSequence(ExpressionSequenceContext ctx) throws ScriptBuildException {
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
	

	
	public static Expression buildSingleExpression(SingleExpressionContext ctx) throws ScriptBuildException {
		SourceInfo uc = sourceInfo(ctx);
		if(ctx instanceof AssignmentExpressionContext) {
			return new Setter((Reference)buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof ArgumentsExpressionContext) {
			return CallableBuilder.buildArgumentsExpression((ArgumentsExpressionContext)ctx);	
		} else if(ctx instanceof FunctionExpressionContext) {
			return CallableBuilder.buildFunctionExpression((FunctionExpressionContext)ctx);
		} else if(ctx instanceof IdentifierExpressionContext) {
			return ReferenceBuilder.buildIdentifier((IdentifierContext)ctx.getChild(0));	
		} else if(ctx instanceof MemberDotExpressionContext) {
			return new MemberDotReference(buildSingleExpressionFromChild(ctx, 0), ctx.getChild(2).getText(), sourceInfo(ctx));
		} else if(ctx instanceof MemberOptionalDotExpressionContext) {
			return new MemberOptionalDotReference(buildSingleExpressionFromChild(ctx, 0), ctx.getChild(2).getText(), sourceInfo(ctx));			
		} else if(ctx instanceof MemberIndexExpressionContext) {
			return new MemberIndexReference(buildSingleExpressionFromChild(ctx, 0), buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(2)).get(0), sourceInfo(ctx));			
		} else if(ctx instanceof LiteralExpressionContext) {
			return LiteralBuilder.buildLiteral((LiteralContext)ctx.getChild(0));
		} else if(ctx instanceof ArrayLiteralExpressionContext) {
			return LiteralBuilder.buildArrayLiteral((ArrayLiteralContext)ctx.getChild(0));
		} else if(ctx instanceof ObjectLiteralExpressionContext) {
			return LiteralBuilder.buildObjectLiteral((ObjectLiteralContext)ctx.getChild(0));
		} else if(ctx instanceof PostIncrementExpressionContext) {
			return new Increase((Reference)buildSingleExpressionFromChild(ctx, 0), uc);
		} else if(ctx instanceof PostDecreaseExpressionContext) {
			return new Decrease((Reference)buildSingleExpressionFromChild(ctx, 0), uc);
		} else if(ctx instanceof PreIncrementExpressionContext) {
			return new PreIncrease((Reference)buildSingleExpressionFromChild(ctx, 1), uc);
		} else if(ctx instanceof PreDecreaseExpressionContext) {
			return new PreDecrease((Reference)buildSingleExpressionFromChild(ctx, 1), uc);
		} else if(ctx instanceof UnaryPlusExpressionContext) {
			return new UnaryPlus(buildSingleExpressionFromChild(ctx, 1), uc);
		} else if(ctx instanceof UnaryMinusExpressionContext) {
			return new UnaryMinus(buildSingleExpressionFromChild(ctx, 1), uc);
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
		} else if(ctx instanceof AdditiveExpressionContext) {
			TerminalNode op = (TerminalNode)ctx.getChild(1);
			if(op.getSymbol().getType() == JavaScriptParser.Plus)
				return new Add(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.getSymbol().getType() == JavaScriptParser.Minus)
				return new Substract(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof AssignmentOperatorExpressionContext) {
			AssignmentOperatorContext aoc = (AssignmentOperatorContext)ctx.getChild(1);
			if(aoc.getText().equals("+=")) 
				return new AddSet((Reference)buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(aoc.getText().equals("-=")) 
				return new SubstractSet((Reference)buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(aoc.getText().equals("*=")) 
				return new MultiplySet((Reference)buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(aoc.getText().equals("/=")) 
				return new DivideSet((Reference)buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof PowerExpressionContext) {
			return new Power(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof CoalesceExpressionContext) {
			return new Coalesce(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof RelationalExpressionContext) {
			String op =  ctx.getChild(1).getText();
			if(op.equals(">"))
				return new GreaterThan(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.equals(">="))
				return new GreaterEqualThan(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.equals("<"))
				return new LessThan(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.equals("<="))
				return new LessEqualThan(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof EqualityExpressionContext) {
			String op = ctx.getChild(1).getText();
			if(op.equals("=="))
				return new Equals(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.equals("!="))
				return new NotEquals(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.equals("==="))
				return new Identical(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
			else if(op.equals("!=="))
				return new NotIdentical(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		} else if(ctx instanceof ParenthesizedExpressionContext) {
			List<Expression> seq = buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(1));
			return seq.get(0);
		} else if(ctx instanceof NewExpressionContext) {
			return CallableBuilder.buildNewOperator((NewExpressionContext)ctx);
		} else if(ctx instanceof DeleteExpressionContext) {
			return new Delete(buildSingleExpressionFromChild(ctx, 1), uc);
		} else if(ctx instanceof ThisExpressionContext) {
			return new VariableReference("this", uc);
		} else if(ctx instanceof TernaryExpressionContext) {
			return new TernaryExpression(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), buildSingleExpressionFromChild(ctx, 4), uc);
		} else if(ctx instanceof TypeofExpressionContext) {
			return new Typeof(buildSingleExpressionFromChild(ctx, 1), uc);
		} else if(ctx instanceof InExpressionContext) {
			return new In(buildSingleExpressionFromChild(ctx, 0), buildSingleExpressionFromChild(ctx, 2), uc);
		}
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	public static Expression buildSingleExpressionFromChild(ParseTree parentContext, int childIndex) throws ScriptBuildException {
		return buildSingleExpression((SingleExpressionContext)parentContext.getChild(childIndex));
	}
}
