package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptParser.AnonymousFunctionContext;
//import io.firebus.script.parser.JavaScriptParser.AnoymousFunctionDeclContext;
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
import io.firebus.script.parser.JavaScriptParser.NewExpressionContext;
import io.firebus.script.parser.JavaScriptParser.SingleExpressionContext;
import io.firebus.script.parser.JavaScriptParser.SourceElementsContext;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.units.expressions.Call;
import io.firebus.script.units.operators.New;
import io.firebus.script.units.setters.CallableDefinition;
import io.firebus.script.units.statements.Block;
import io.firebus.script.units.statements.Return;

public class CallableBuilder extends Builder {

	public static Expression buildArgumentsExpression(ArgumentsExpressionContext ctx) throws ScriptBuildException {
		Expression callable = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)ctx.getChild(0));
		List<Expression> args = buildArguments((ArgumentsContext)ctx.getChild(1));
		return new Call(callable, args, sourceInfo(ctx));
	}
	
	public static List<Expression> buildArguments(ArgumentsContext ctx) throws ScriptBuildException {
		List<Expression> list = new ArrayList<Expression>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof ArgumentContext) {
				list.add(buildArgument((ArgumentContext)sub));
			}
		}
		return list;
	}
	
	public static Expression buildArgument(ArgumentContext ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof SingleExpressionContext) {
			return ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub);
		} else if(sub instanceof IdentifierContext) {
			return ReferenceBuilder.buildIdentifier((IdentifierContext)sub);
		}
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	
	
	public static CallableDefinition buildFunctionExpression(FunctionExpressionContext ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof ArrowFunctionContext) {
			List<String> params = buildArrowFunctionParameters((ArrowFunctionParametersContext)sub.getChild(0));
			Block body = buildArrowFunctionBody((ArrowFunctionBodyContext)sub.getChild(2));
			CallableDefinition callDef = new CallableDefinition(null, params, body, sourceInfo(ctx));
			return callDef;
		} else if(sub instanceof AnonymousFunctionContext) {
			List<String> params = null;
			Block body = null;
			if(sub.getChildCount() ==  4) {
				params = new ArrayList<String>();
				body = buildFunctionBody((FunctionBodyContext)sub.getChild(3));
			} else if(sub.getChildCount() == 5) {
				params = buildFormalParameterList((FormalParameterListContext)sub.getChild(2));
				body = buildFunctionBody((FunctionBodyContext)sub.getChild(4));
			}
			CallableDefinition callDef = new CallableDefinition(null, params, body, sourceInfo(ctx));
			return callDef;			
		}
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	public static List<String> buildArrowFunctionParameters(ArrowFunctionParametersContext ctx) throws ScriptBuildException {
		if(ctx.getChild(0) instanceof TerminalNodeImpl) {
			if(ctx.getChildCount() == 3) {
				return buildFormalParameterList((FormalParameterListContext)ctx.getChild(1));
			} else if(ctx.getChildCount() == 2) {
				return new ArrayList<String>();
			} else {
				throw new ScriptBuildException("Error building arrow function parameters", sourceInfo(ctx));
			}			
		} else {
			List<String> list = new ArrayList<String>();
			list.add(ctx.getChild(0).getText());
			return list;
		}

	}
	
	public static Block buildArrowFunctionBody(ArrowFunctionBodyContext ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof FunctionBodyContext) {
			return buildFunctionBody((FunctionBodyContext)sub);
		} else if(sub instanceof SingleExpressionContext) {
			SourceInfo uc = sourceInfo(ctx);
			Expression expr = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub);
			Return ret = new Return(expr, uc);
			List<Statement> list = new ArrayList<Statement>();
			list.add(ret);
			Block block = new Block(list, uc);
			return block;
		}
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	public static Block buildFunctionBody(FunctionBodyContext ctx) throws ScriptBuildException {
		List<Statement> list = MasterBuilder.buildSourceElements((SourceElementsContext)ctx.getChild(1));
		return new Block(list, sourceInfo(ctx));
	}
	
	public static List<String> buildFormalParameterList(FormalParameterListContext ctx) throws ScriptBuildException {
		List<String> list = new ArrayList<String>();
		for(ParseTree sub: ctx.children) {
			if(sub instanceof FormalParameterArgContext) {
				list.add(sub.getText());				
			}
		}
		return list;
	}
	
	public static New buildNewOperator(NewExpressionContext ctx) throws ScriptBuildException {
		Expression callable = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)ctx.getChild(1));
		List<Expression> args = buildArguments((ArgumentsContext)ctx.getChild(2));
		return new New(callable, args, sourceInfo(ctx));
	}
}
