package io.firebus.script.values.flow;


public class SBreak extends SFlow {
	
	private static SBreak singleton = new SBreak();
	
	private SBreak() {
		
	}
	
	public static SBreak get() {
		return singleton;
	}

	public String toString() {
		return "break";
	}

}
