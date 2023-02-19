package io.firebus.adapters.http.decoders;

public class ContentType {
	public String value;
	public String boundary;
	
	public ContentType(String s) {
		String[] parts = s.split(";");
		value = parts[0].toLowerCase();
		for(int i = 1; i < parts.length; i++) {
			String[] subparts = parts[i].split("=");
			if(subparts[0].trim().equalsIgnoreCase("boundary")) {
				boundary = subparts[1].trim();
			}
		}
	}
}
