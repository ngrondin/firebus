package io.firebus.adapters.http.decoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.firebus.adapters.http.Tools;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public class MultipartFormDecoder {

	public static Object decode(InputStream is, String boundary) throws IOException, DataException {
		DataMap map = new DataMap();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Tools.pipeStreams(is, baos);
		byte[] bytes = baos.toByteArray();
		byte[] boundbytes = boundary.getBytes();
		byte[] lastbytes = new byte[boundbytes.length];
		int lastStart = 0;
		for(int i = 0; i < bytes.length - boundbytes.length; i++) {
			boolean boundStart = true;
			for(int j = 0; j < boundbytes.length; j++) {
				if(bytes[i + j] != boundbytes[j]) {
					boundStart = false;
					break;
				}
			}
			if(boundStart) {
				String contentType = null;
				String fieldName = null;
				String fileName = null;
				Object fieldValue = null;
				for(int j = lastStart; j < i - 1; j++) {
					if(bytes[j] == '\n' && bytes[j + 1] == '\n') {
						String headerString = new String(bytes, lastStart, j - lastStart);
						String[] headerLines = headerString.split("\n");
						for(String headerLine: headerLines) {
							int colonPos = headerLine.indexOf(":");
							String headerName = headerLine.substring(0, colonPos);
							String headerValue = headerLine.substring(colonPos);
							if(headerName.equalsIgnoreCase("content-disposition")) {
								String[] contentDispoParts = headerValue.split(";");
								String type = null;
								for(String contentDispoPart : contentDispoParts) {
									if(contentDispoPart.equalsIgnoreCase("form-data")) {
										type = "form-data";
									} else if(contentDispoPart.indexOf("=") > -1) {
										String[] subparts = contentDispoPart.split("=");
										if(subparts[0].equals("name")) {
											fieldName = stripQuotes(subparts[1]);
										} else if(subparts[0].equals("filename")) {
											fileName = stripQuotes(subparts[1]);
										}
									}
								}
							}
						}
						break;
					}
				}
			}
		}
		return map;
	}
	
	protected static String stripQuotes(String in) {
		String out = in;
		if(in.charAt(0) == '"') out = out.substring(1);
		if(in.charAt(in.length() - 1) == '"') out = out.substring(0, out.length() - 1);
		return out;
	}
}
