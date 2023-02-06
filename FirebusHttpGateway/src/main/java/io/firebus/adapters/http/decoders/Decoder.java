package io.firebus.adapters.http.decoders;

import java.io.InputStream;

public abstract class Decoder {

	public static Object decode(String mime, InputStream is) throws DecoderException {
		try {
			switch(mime) {
				case "application/json":
					return JsonDecoder.decode(is);
				case "application/x-www-form-urlencoded":
					return URLEncodedFormDecoder.decode(is);
				case "multipart/form-data":
					return MultipartDecoder.decode(is, "");					
				default:
					return BytesDecoder.decode(is);
			}
		} catch(Exception e) {
			throw new DecoderException("Error decoding http body", e);
		}
	}
}
