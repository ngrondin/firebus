package io.firebus.utils;

import java.util.Arrays;

public class StackUtils {
	
	public static String getStackString() {
		StackTraceElement[] elements = Thread.getAllStackTraces().get(Thread.currentThread());
		int start = 0;
		while(!elements[start].getMethodName().equals("getStackString"))
			start++;
		StackTraceElement[] output = Arrays.copyOfRange(elements, start + 1, elements.length);
		return toString(output);
	}
	
	
	public static String toString(StackTraceElement[] elements) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < elements.length; i++) {
			if(sb.length() > 0) sb.append("\r\n");
			sb.append(elements[i].getClassName());
			sb.append(".");
			sb.append(elements[i].getMethodName());
			sb.append("(");
			sb.append(elements[i].getFileName());
			sb.append(":");
			sb.append(elements[i].getLineNumber());
			sb.append(")");
		}
		return sb.toString();		
	}

}
