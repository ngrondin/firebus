package io.firebus.script.builder;

import org.antlr.v4.runtime.tree.ParseTree;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.BuildException;
import io.firebus.script.parser.JavaScriptParser.IdentifierContext;
import io.firebus.script.units.references.Reference;
import io.firebus.script.units.references.VariableReference;

public class ReferenceBuilder extends Builder {
	public static String buildAssignable(ParseTree ctx) throws BuildException {
		return ctx.getChild(0).getText(); 
		//TODO: This is not right;
		
	}
	
	public static Reference buildIdentifier(IdentifierContext ctx) throws BuildException {
		SourceInfo uc = sourceInfo(ctx);
		return new VariableReference(ctx.getText(), uc);
	}
}
