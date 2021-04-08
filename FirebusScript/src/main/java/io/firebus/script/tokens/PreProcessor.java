package io.firebus.script.tokens;

public class PreProcessor {

	public static String preprocess(String str) {
		int pos1 = -1;
		String out = str;
		while((pos1 = out.indexOf("/*")) > -1) {
			int pos2 = out.indexOf("*/");
			out = out.substring(0, pos1) + out.substring(pos2);
		}
		return out;
	}
}
