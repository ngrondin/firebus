package io.firebus.script;

import org.antlr.v4.runtime.ANTLRInputStream;


@SuppressWarnings("deprecation")
public class Source extends ANTLRInputStream {
	protected String name;
	
	public Source(String n, String b) {
		super(b != null ? b : "");
		name = n;
	}

	public String getSourceName() {
		return name;
	}


}
