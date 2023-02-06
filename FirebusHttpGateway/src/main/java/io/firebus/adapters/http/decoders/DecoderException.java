package io.firebus.adapters.http.decoders;

public class DecoderException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public DecoderException(String m, Throwable t) {
		super(m, t);
	}

}
