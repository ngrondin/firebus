package io.firebus.adapters.http.decoders;

import java.io.InputStream;

public abstract class Decoder {

	public static Object decode(InputStream is, String contentType) throws DecoderException {
		try {
			ContentType ct = new ContentType(contentType);
			if(ct.value.equals("application/json")) {
				return JsonDecoder.decode(is);
			} else if(ct.value.equals("application/x-www-form-urlencoded")) {
				return URLEncodedFormDecoder.decode(is);
			} else if(ct.value.equals("multipart/form-data")) {
				return MultipartFormDecoder.decode(is, ct.boundary);	
			} else  {
				return BytesDecoder.decode(is);
			} 

		} catch(Exception e) {
			throw new DecoderException("Error decoding http body", e);
		}
	}
}
