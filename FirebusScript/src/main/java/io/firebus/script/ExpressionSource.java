package io.firebus.script;

public class ExpressionSource extends Source {

	public ExpressionSource(String n, String b) {
		super(n, preProcessBody(b));
	}

	private static String preProcessBody(String b) {
		String src = b != null ? b.trim() : "null";
		if(src.startsWith("{") && src.endsWith("}"))
			src = "(" + src + ")";	
		return src;
	}
}
