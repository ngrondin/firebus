package io.firebus.script.builder;

import org.antlr.v4.runtime.tree.ParseTree;

import io.firebus.script.parser.JavaScriptParser.IdentifierContext;
import io.firebus.script.units.Reference;
import io.firebus.script.units.UnitContext;

public class ReferenceBuilder {
	public static String buildAssignable(ParseTree ctx) {
		return ctx.getChild(0).getText(); 
		//TODO: This is not right;
		
	}
	
	public static Reference buildIdentifier(IdentifierContext ctx) {
		UnitContext uc = new UnitContext(ctx.getStart().getTokenSource().getSourceName(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
		return new Reference(ctx.getText(), uc);
	}
}
