package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptParser.SourceElementContext;
import io.firebus.script.parser.JavaScriptParser.SourceElementsContext;
import io.firebus.script.parser.JavaScriptParser.StatementContext;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.units.statements.Block;

public class MasterBuilder extends Builder {


	
	public static Block buildProgram(ParseTree ctx) throws ScriptBuildException {
		List<Statement> list = null;
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof TerminalNode) {
			list = new ArrayList<Statement>();
		} else {
			list = buildSourceElements((SourceElementsContext)sub);
		}
		return new Block(list, sourceInfo((ParserRuleContext)ctx));
	}
	
	public static List<Statement> buildSourceElements(SourceElementsContext ctx) throws ScriptBuildException {
		List<Statement> list = new ArrayList<Statement>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof SourceElementContext) {
				list.add(buildSourceElement((SourceElementContext)sub));
			}
		}
		return list;
	}
	
	public static Statement buildSourceElement(SourceElementContext ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof StatementContext) {
			return StatementBuilder.buildStatement((StatementContext)sub);
		} 
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	

}
