package io.firebus.script;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import io.firebus.script.builder.MasterBuilder;
import io.firebus.script.exceptions.BuildException;
import io.firebus.script.parser.JavaScriptLexer;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.units.ExecutionUnit;

public class Compiler {

	public ExecutionUnit compile(String source) throws BuildException {
		JavaScriptLexer lexer = new JavaScriptLexer(CharStreams.fromString(source));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaScriptParser parser = new JavaScriptParser(tokens);
		JavaScriptParser.ProgramContext tree = parser.program();
		MasterBuilder builder = new MasterBuilder();
		ExecutionUnit root = builder.buildProgram(tree);
		return root;
	}
}
