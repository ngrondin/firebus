package io.firebus.adapters.http.decoders;

import java.io.IOException;
import java.io.InputStream;

import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public class JsonDecoder extends Decoder {

	public static DataMap decode(InputStream is) throws IOException, DataException {
		return new DataMap(is);
	}
}
