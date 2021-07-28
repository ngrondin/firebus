package io.firebus.script.builder;

import org.antlr.v4.runtime.ParserRuleContext;

import io.firebus.script.SourceInfo;

public class Builder {
	
	public static SourceInfo sourceInfo(ParserRuleContext ctx) {
		return new SourceInfo(ctx.getStart().getTokenSource().getSourceName(), ctx.getText(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
	}
}
