package io.firebus.script.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import io.firebus.script.builder.JavaScriptUnitBuilder;
import io.firebus.script.parser.JavaScriptLexer;
import io.firebus.script.parser.JavaScriptParser;

public class ParseTest {

	public static void main(String[] args) {
		try {

			JavaScriptLexer lexer = new JavaScriptLexer(CharStreams.fromFileName("test.js"));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JavaScriptParser parser = new JavaScriptParser(tokens);
			JavaScriptParser.ProgramContext tree = parser.program();
			JavaScriptUnitBuilder builder = new JavaScriptUnitBuilder();
			ParseTreeWalker.DEFAULT.walk(builder, tree);
			System.out.println("Done");
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
}
