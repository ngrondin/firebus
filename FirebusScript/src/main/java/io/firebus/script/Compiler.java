package io.firebus.script;

import io.firebus.script.tokens.Cleaner;
import io.firebus.script.tokens.Grouper;
import io.firebus.script.tokens.TokenList;
import io.firebus.script.tokens.Tokenizer;

public class Compiler {

	public void compile(String name, String str) {
		TokenList tokens = Tokenizer.tokenize("test", str);
		Cleaner.clean(tokens);
		Grouper.group(tokens, "{" ,"}");
		Grouper.group(tokens, "(" ,")");
		System.out.println(tokens);
	}
}
