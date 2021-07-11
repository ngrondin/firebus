package io.firebus.script;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import io.firebus.script.builder.AllBuilder;
import io.firebus.script.parser.JavaScriptLexer;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.units.ExecutionUnit;

public class Compiler {

	public ExecutionUnit compile(String source) {
		JavaScriptLexer lexer = new JavaScriptLexer(CharStreams.fromString(source));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaScriptParser parser = new JavaScriptParser(tokens);
		JavaScriptParser.ProgramContext tree = parser.program();
		AllBuilder builder = new AllBuilder();
		ExecutionUnit root = builder.buildProgram(tree);
		return root;
	}
}
