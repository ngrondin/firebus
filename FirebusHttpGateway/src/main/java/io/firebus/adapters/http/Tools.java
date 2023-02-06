package io.firebus.adapters.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import io.firebus.Payload;

public class Tools {

	public static void enrichFirebusRequestDefault(HttpRequest req, Payload payload) {

		for(String name : req.getHeaderNames()) {
			if(name.toLowerCase().startsWith("firebus-")) {
				String shortName = name.toLowerCase().substring(8);
				List<String> values = req.getHeader(name);
				if(values.size() > 0)
					payload.metadata.put(shortName, values.get(0));
			}
		}
		
		for(String name : req.getParameterNames()) {
			if(name.toLowerCase().startsWith("firebus-")) {
				String shortName = name.toLowerCase().substring(8);
				payload.metadata.put(shortName, req.getParameter(name));
			}
		}
	}
	
	public static void pipeStreams(InputStream is, OutputStream os) throws IOException {
	    int read;
	    byte[] data = new byte[1024];
	    while ((read = is.read(data, 0, data.length)) != -1)
	    	os.write(data, 0, read);
	    os.flush();
	}
}
