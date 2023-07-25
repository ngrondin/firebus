package io.firebus.script.values.flow;

public class SSkipExpression extends SFlow {
	private static SSkipExpression singleton = new SSkipExpression();
	
	private SSkipExpression() {
		
	}
	
	public static SSkipExpression get() {
		return singleton;
	}


	public String toString() {
		return "skip statement";
	}

}
