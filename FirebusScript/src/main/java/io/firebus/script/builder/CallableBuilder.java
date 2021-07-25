package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.antlr.v4.runtime.tree.ParseTree;

import io.firebus.script.parser.JavaScriptParser.ArgumentContext;
import io.firebus.script.parser.JavaScriptParser.ArgumentsContext;
import io.firebus.script.parser.JavaScriptParser.ArgumentsExpressionContext;
import io.firebus.script.parser.JavaScriptParser.ArrowFunctionBodyContext;
import io.firebus.script.parser.JavaScriptParser.ArrowFunctionContext;
import io.firebus.script.parser.JavaScriptParser.ArrowFunctionParametersContext;
import io.firebus.script.parser.JavaScriptParser.FormalParameterArgContext;
import io.firebus.script.parser.JavaScriptParser.FormalParameterListContext;
import io.firebus.script.parser.JavaScriptParser.FunctionBodyContext;
import io.firebus.script.parser.JavaScriptParser.FunctionExpressionContext;
import io.firebus.script.parser.JavaScriptParser.IdentifierContext;
import io.firebus.script.parser.JavaScriptParser.SingleExpressionContext;
import io.firebus.script.parser.JavaScriptParser.SourceElementsContext;
import io.firebus.script.units.Block;
import io.firebus.script.units.Call;
import io.firebus.script.units.CallableDefinition;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.statements.Return;

public class CallableBuilder {

	public static Expression buildArgumentsExpression(ArgumentsExpressionContext ctx) {
		Expression callable = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)ctx.getChild(0));
		List<Expression> args = buildArguments((ArgumentsContext)ctx.getChild(1));
		return new Call(callable, args, ContextBuilder.buildContext(ctx));
	}
	
	public static List<Expression> buildArguments(ArgumentsContext ctx) {
		List<Expression> list = new ArrayList<Expression>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof ArgumentContext) {
				list.add(buildArgument((ArgumentContext)sub));
			}
		}
		return list;
	}
	
	public static Expression buildArgument(ArgumentContext ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof SingleExpressionContext) {
			return ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub);
		} else if(sub instanceof IdentifierContext) {
			return ReferenceBuilder.buildIdentifier((IdentifierContext)sub);
		} else {
			return null;
		}
	}
	
	
	
	public static CallableDefinition buildFunctionExpression(FunctionExpressionContext ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof ArrowFunctionContext) {
			return buildArrowFunction((ArrowFunctionContext)sub);
		} else {
			return null;
		}
	}
	
	public static CallableDefinition buildArrowFunction(ArrowFunctionContext ctx) {
		List<String> params = buildArrowFunctionParameters((ArrowFunctionParametersContext)ctx.getChild(0));
		Block body = buildArrowFunctionBody((ArrowFunctionBodyContext)ctx.getChild(2));
		CallableDefinition callDef = new CallableDefinition(params, body, ContextBuilder.buildContext(ctx));
		return callDef;
	}
	
	public static List<String> buildArrowFunctionParameters(ArrowFunctionParametersContext ctx) {
		return buildFormalParameterList((FormalParameterListContext)ctx.getChild(1));
	}
	
	public static Block buildArrowFunctionBody(ArrowFunctionBodyContext ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof FunctionBodyContext) {
			return buildFunctionBody((FunctionBodyContext)sub);
		} else if(sub instanceof SingleExpressionContext) {
			UnitContext uc = ContextBuilder.buildContext(ctx);
			Expression expr = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub);
			Return ret = new Return(expr, uc);
			List<ExecutionUnit> list = new ArrayList<ExecutionUnit>();
			list.add(ret);
			Block block = new Block(list, uc);
			return block;
		} else {
			return null;
		}
	}
	
	public static Block buildFunctionBody(FunctionBodyContext ctx) {
		return MasterBuilder.buildSourceElements((SourceElementsContext)ctx.getChild(1));
	}
	
	public static List<String> buildFormalParameterList(FormalParameterListContext ctx) {
		List<String> list = new ArrayList<String>();
		for(ParseTree sub: ctx.children) {
			if(sub instanceof FormalParameterArgContext) {
				list.add(sub.getText());				
			}
		}
		return list;
	}
	
}
