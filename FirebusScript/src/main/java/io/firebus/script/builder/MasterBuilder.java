package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import io.firebus.script.units.Block;
import io.firebus.script.units.Statement;
import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptParser.*;

public class MasterBuilder extends Builder {


	
	public static Block buildProgram(ParseTree ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		return buildSourceElements((SourceElementsContext)sub);
	}
	
	public static Block buildSourceElements(SourceElementsContext ctx) throws ScriptBuildException {
		List<Statement> list = new ArrayList<Statement>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof SourceElementContext) {
				list.add(buildSourceElement((SourceElementContext)sub));
			}
		}
		return new Block(list, sourceInfo(ctx));
	}
	
	public static Statement buildSourceElement(SourceElementContext ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof StatementContext) {
			return StatementBuilder.buildStatement((StatementContext)sub);
		} 
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	

}
