package io.firebus.data.parse;

public class StringDecoder {

	
	public static Object decodeQuotedString(String str) {
		Object ret = null;
		ret = DateParser.parse(str);
		if(ret != null) return ret;
		ret = TimeParser.parse(str);
		if(ret != null) return ret;
		return str;
	}
	
	public static Object decodeUnquotedString(String str) {
		Object ret = null;
		ret = NumberParser.parse(str);
		if(ret != null) return ret;
		return str;
	}
}
