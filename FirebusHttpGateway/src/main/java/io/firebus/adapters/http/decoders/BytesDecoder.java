package io.firebus.adapters.http.decoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.firebus.adapters.http.Tools;
import io.firebus.data.DataException;

public class BytesDecoder extends Decoder {

	public static byte[] decode(InputStream is) throws IOException, DataException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Tools.pipeStreams(is, baos);
		return baos.toByteArray();
	}
}
