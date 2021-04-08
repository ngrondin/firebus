package io.firebus.script.tokens;

public class UnitToken extends Token {
	protected static String[] reservedKeywords = {"break","case","catch","class","const","continue","debugger","default","delete","do","else","export","extends","finally","for","function","if","import","in","instanceof","new","return","super","switch","this","throw","try","typeof","var","void","while","with","yield"};
	protected String source;
	
	
	public UnitToken(String sourceName, int line, int column, String str) {
		super(sourceName, line, column);
		source = str;
	}
	
	
	public String getSource() {
		return source;
	}
	
	public boolean is(String str) {
		return source.equals(str);
	}
	
	public boolean isSetterKeyword() {
		return source.equals("var") || source.equals("let") || source.equals("const");
	}

	public boolean isSetterSymbol() {
		return source.equals("=");
	}
	
	public boolean isReservedKeyword() {
		boolean ret = false;
		for(int i = 0; i < reservedKeywords.length; i++)
			if(reservedKeywords[i].equals(source))
				ret = true;
		return ret;
	}
	
	public boolean isString() {
		return (source.startsWith("\"") && source.endsWith("\"")) || (source.startsWith("'") && source.endsWith("'"));
	}

	public boolean isName() {
		if(isReservedKeyword())
			return false;
		if(!Character.isLetter(source.charAt(0)))
			return false;
		for(int i = 1; i < source.length(); i++)
			if(!Character.isLetter(source.charAt(i)) && !Character.isDigit(source.charAt(i)) && source.charAt(i) != '_')
				return false;
		return true;
	}

	public boolean isNumber() {
		boolean ret = true;
		for(int i = 0; i < source.length(); i++)
			if(!Character.isDigit(source.charAt(i)) && source.charAt(i) != '.')
				ret = false;
		return ret;
	}
	
	public String toString() {
		String ret = (source.equals("\n") ? "\\n" : source);
		//ret += " [" + sourceName + " " + line + ", " + column + "]";
		return ret;
	}
}
