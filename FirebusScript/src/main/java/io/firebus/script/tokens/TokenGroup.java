package io.firebus.script.tokens;

public class TokenGroup extends Token {
	protected TokenList tokens;
	protected String type;

	public TokenGroup(String t, TokenList list) {
		super(
			list.size() > 0 ? list.get(0).getSourceFileName() : null,
			list.size() > 0 ? list.get(0).getLine() : 0,
			list.size() > 0 ? list.get(0).getColumn() : 0
		);
		type = t;
		tokens = list;
	}

	public boolean is(String str) {
		return str.equals(type);
	}
	
	public String toString() {
		String str = type.substring(0, 1);
		if(tokens.size() > 0) {
			str += "\n\t" + tokens.toString().replace("\n", "\n\t");
		}
		str += "\n" + type.substring(1);
		return str;
	}
	
}
