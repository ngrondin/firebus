package io.firebus.script.tokens;

public class Tokenizer {
	
	public static TokenList tokenize(String name, String str) {
		String source = str.replace("\r", "").replace("\t", "");
		TokenList tokens = new TokenList();
		int line = 1;
		int lineStart = 1;
		int column = 0;
		int columnStart = 0;
		StringBuilder acc = new StringBuilder();
		boolean inSingleQuote = false; 
		boolean inDoubleQuote = false; 
		boolean inAlphaNum = false;
		boolean brNext = false;
		for(int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			boolean isAlphaNum = Character.isLetter(c) || Character.isDigit(c);
			boolean br = false;
			if(brNext) {
				brNext = false;
				br = true;
			}
			
			if(inSingleQuote || inDoubleQuote) {
				if(c == '\'' && inSingleQuote) {
					inSingleQuote = false;
					brNext = true;
				} else if(c == '"' && inDoubleQuote) {
					inDoubleQuote = false;
					brNext = true;
				}
			} else {
				if(c == '\'') {
					br = true;
					inSingleQuote = true;
				} else if(c == '"') {
					br = true;
					inDoubleQuote = true;
				} else if(isAlphaNum && !inAlphaNum) {
					br = true;
					inAlphaNum = true;
				} else if(!isAlphaNum && inAlphaNum) {
					br = true;
					inAlphaNum = false;
				} else if(specialBreakChar(c)) {
					br = true;
					brNext = true;
				}
			}
			
			if(br) {
				tokens.pushToken(name, lineStart, columnStart, acc.toString());
				acc = new StringBuilder();
				lineStart = line;
				columnStart = column;
			} 
			acc.append(c);
			column++;
			if(c == '\n') {
				line++;
				column = 0;
			}
		}
		tokens.pushToken(name, lineStart, column, acc.toString());
		return tokens;
	}
	
	
	public static boolean specialBreakChar(char c) {
		return c == '(' || c == ')' || c == ';' || c == ' ' || c == '\n';
	}
	
	

}
