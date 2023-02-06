package io.firebus.adapters.http.decoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.firebus.adapters.http.Tools;
import io.firebus.data.DataException;

public class MultipartDecoder extends Decoder {

	public static List<Object> decode(InputStream is, String boundary) throws IOException, DataException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Tools.pipeStreams(is, baos);
		byte[] bytes = baos.toByteArray();
		byte[] boundbytes = boundary.getBytes();
		byte[] lastbytes = new byte[boundbytes.length];
		for(int start = 0; start < bytes.length - boundbytes.length; start++) {
			
		}
		return null;
	}
}
