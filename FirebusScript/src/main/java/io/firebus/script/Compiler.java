package io.firebus.script;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;

import io.firebus.script.builder.MasterBuilder;
import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptLexer;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.units.ExecutionUnit;

public class Compiler {

	public ExecutionUnit compile(String source) throws ScriptBuildException {
		JavaScriptLexer lexer = new JavaScriptLexer(CharStreams.fromString(source));
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaScriptParser parser = new JavaScriptParser(tokens);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		ErrorListener el = new ErrorListener();
		parser.addErrorListener(el);
		JavaScriptParser.ProgramContext tree = parser.program();
		if(el.getErrorCount() == 0) {
			try {
				ExecutionUnit root = MasterBuilder.buildProgram(tree);
				return root;
			} catch(ScriptBuildException e) {
				throw new ScriptBuildException(e.getMessageText(), e.getSourceInfo());
			}
		} else {
			StringBuilder sb = new StringBuilder();
			for(String error: el.getErrors()) {
				sb.append(error);
				sb.append("\r\n");
			}
			throw new ScriptBuildException(sb.toString().trim());
		}
	}
}
