package io.firebus.utils.jwt;

public class JWTValidatorException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public JWTValidatorException(String m) {
		super(m);
	}
	
	public JWTValidatorException(String m, Throwable t) {
		super(m, t);
	}

}
