package io.firebus.adapters.http.decoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import io.firebus.adapters.http.Tools;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public class URLEncodedFormDecoder extends Decoder {

	public static DataMap decode(InputStream is) throws IOException, DataException {
		DataMap map = new DataMap();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Tools.pipeStreams(is, baos);
		String encodedStr = new String(baos.toByteArray());
		String[] parts = encodedStr.split("\\&");
		for(String encodedPart : parts) {
			String[] subparts = encodedPart.split("=");
			String name = URLDecoder.decode(subparts[0], "UTF-8");
		    String value = URLDecoder.decode(subparts[1], "UTF-8");
		    map.put(name, value);
		}
		return map;
	}
}
