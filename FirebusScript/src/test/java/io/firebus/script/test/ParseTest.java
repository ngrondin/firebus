package io.firebus.script.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import io.firebus.script.builder.JavaScriptUnitBuilder;
import io.firebus.script.builder.UnitBuilder;
import io.firebus.script.parser.JavaScriptLexer;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.values.impl.Print;

public class ParseTest {

	public static void main(String[] args) {
		try {

			JavaScriptLexer lexer = new JavaScriptLexer(CharStreams.fromFileName("test.js"));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JavaScriptParser parser = new JavaScriptParser(tokens);
			JavaScriptParser.ProgramContext tree = parser.program();
			UnitBuilder builder = new UnitBuilder();
			ExecutionUnit root = builder.buildProgram(tree);
			Print p = new Print();
			Scope s = new Scope();
			s.setValue("print", p);
			root.eval(s);
			System.out.println("Done");
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
}
