package io.firebus.utils.jwt;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

public class JWTValidatorException extends Exception {
	
	private static final long serialVersionUID = 1L;
	public boolean expired = false;
	public boolean badAlgorithm = false;
	public boolean badSignature = false;

	public JWTValidatorException(String m) {
		super(m);
	}
	
	public JWTValidatorException(String m, Throwable t) {
		super(m, t);
		expired = t instanceof TokenExpiredException;
		badAlgorithm = t instanceof AlgorithmMismatchException;
		badSignature = t instanceof SignatureVerificationException;
	}

}
