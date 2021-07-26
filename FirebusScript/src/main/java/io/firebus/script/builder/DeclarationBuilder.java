package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import io.firebus.script.parser.JavaScriptParser.FormalParameterListContext;
import io.firebus.script.parser.JavaScriptParser.FunctionBodyContext;
import io.firebus.script.parser.JavaScriptParser.FunctionDeclarationContext;
import io.firebus.script.parser.JavaScriptParser.SingleExpressionContext;
import io.firebus.script.parser.JavaScriptParser.VarModifierContext;
import io.firebus.script.parser.JavaScriptParser.VariableDeclarationContext;
import io.firebus.script.parser.JavaScriptParser.VariableDeclarationListContext;
import io.firebus.script.units.Block;
import io.firebus.script.units.CallableDefinition;
import io.firebus.script.units.Declare;
import io.firebus.script.units.DeclareList;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;

public class DeclarationBuilder {

	public static DeclareList buildVariableDeclarationList(VariableDeclarationListContext ctx) {
		UnitContext uc = new UnitContext(ctx.getStart().getTokenSource().getSourceName(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());		
		List<Declare> list = new ArrayList<Declare>();
		String modifier = null;
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof VarModifierContext) {
				modifier = sub.getText();
			} else if(sub instanceof VariableDeclarationContext) {
				list.add(buildVariableDeclaration((VariableDeclarationContext)sub));
			}
		}
		return new DeclareList(modifier, list, uc);
	}
	
	public static Declare buildVariableDeclaration(VariableDeclarationContext ctx) {
		String key = ReferenceBuilder.buildAssignable(ctx.getChild(0));
		ParseTree sub = ctx.getChild(2);
		Expression exp = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub);
		return new Declare(key, exp, ContextBuilder.buildContext(ctx));
	}
	
	public static Declare buildFunctionDeclaration(FunctionDeclarationContext ctx) {
		String key = ctx.getChild(1).getText();
		List<String> params = null;
		if(ctx.getChildCount() > 5) {
			params = CallableBuilder.buildFormalParameterList((FormalParameterListContext)ctx.getChild(3));
		} else {
			params = new ArrayList<String>();
		}
		Block body = CallableBuilder.buildFunctionBody((FunctionBodyContext)ctx.getChild(ctx.getChildCount() - 1));
		CallableDefinition callDef = new CallableDefinition(params, body, ContextBuilder.buildContext(ctx));
		return new Declare(key, callDef, ContextBuilder.buildContext(ctx));
	}
}
