package io.firebus.adapters.http.decoders;

public class DecodedFile {
	public byte[] body;
	public String filename;
	
	public DecodedFile(byte[] b, String fn) {
		body = b;
		filename = fn;
	}

}
