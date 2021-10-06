package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptParser.FormalParameterListContext;
import io.firebus.script.parser.JavaScriptParser.FunctionBodyContext;
import io.firebus.script.parser.JavaScriptParser.FunctionDeclarationContext;
import io.firebus.script.parser.JavaScriptParser.SingleExpressionContext;
import io.firebus.script.parser.JavaScriptParser.VarModifierContext;
import io.firebus.script.parser.JavaScriptParser.VariableDeclarationContext;
import io.firebus.script.parser.JavaScriptParser.VariableDeclarationListContext;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.setters.CallableDefinition;
import io.firebus.script.units.setters.Declare;
import io.firebus.script.units.setters.DeclareList;
import io.firebus.script.units.statements.Block;

public class DeclarationBuilder extends Builder {

	public static DeclareList buildVariableDeclarationList(VariableDeclarationListContext ctx) throws ScriptBuildException {
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
		return new DeclareList(modifier, list, sourceInfo(ctx));
	}
	
	public static Declare buildVariableDeclaration(VariableDeclarationContext ctx) throws ScriptBuildException {
		String key = ReferenceBuilder.buildAssignable(ctx.getChild(0));
		Expression valExpr = null;
		if(ctx.getChildCount() >= 3) {
			valExpr = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)ctx.getChild(2));
		}
		return new Declare(key, valExpr, sourceInfo(ctx));
	}
	
	public static Declare buildFunctionDeclaration(FunctionDeclarationContext ctx) throws ScriptBuildException {
		String key = ctx.getChild(1).getText();
		List<String> params = null;
		if(ctx.getChildCount() > 5) {
			params = CallableBuilder.buildFormalParameterList((FormalParameterListContext)ctx.getChild(3));
		} else {
			params = new ArrayList<String>();
		}
		Block body = CallableBuilder.buildFunctionBody((FunctionBodyContext)ctx.getChild(ctx.getChildCount() - 1));
		CallableDefinition callDef = new CallableDefinition(key, params, body, sourceInfo(ctx));
		Declare declare = new Declare(key, callDef, sourceInfo(ctx));
		declare.setModifier("function");
		return declare;
	}
}
