package io.firebus.script.tokens;

import java.util.ArrayList;

public class TokenList extends ArrayList<Token> {
	private static final long serialVersionUID = 1L;
	
	protected void pushToken(String sourceName, int line, int column, String s) {
		String str = s;
		while(str.endsWith(" ")) str = str.substring(0, str.length() - 1);
		while(str.startsWith(" ")) str = str.substring(1);
		if(str.length() > 0) {
			Token token = new UnitToken(sourceName, line, column, str);
			add(token);
		}
	}
	
	public boolean tokenIs(int i, String str) {
		if(i > 0 && i < size()) {
			Token t = get(i);
			if(t != null) {
				return t.is(str);
			} else {
				return false;
			}
		} else {
			return false;
		}		
	}
	
	public TokenList slice(int start, int end) {
		TokenList sublist = new TokenList();
		for(int j = start; j <= end; j++) {
			sublist.add(get(j));
		}
		for(int j = 0; j < end - start + 1; j++) {
			remove(start);
		}
		return sublist;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Token token : this) {
			if(sb.length() > 0)
				sb.append("\r\n");
			sb.append(token);
		}
		return sb.toString();
	}
	
}
