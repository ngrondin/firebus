package io.firebus.adapters.http;

public class FirebusHttpException extends Exception {
	public int code;
	
	public FirebusHttpException(String msg, int c, Throwable t) {
		super(msg, t);
		code = c;
	}
	
	public int getErrorCode() {
		return code;
	}

}
