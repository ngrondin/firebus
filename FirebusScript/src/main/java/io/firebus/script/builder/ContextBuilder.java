package io.firebus.script.builder;

import org.antlr.v4.runtime.ParserRuleContext;

import io.firebus.script.units.UnitContext;

public class ContextBuilder {

	public static UnitContext buildContext(ParserRuleContext ctx) {
		return new UnitContext(ctx.getStart().getTokenSource().getSourceName(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
	}
}
