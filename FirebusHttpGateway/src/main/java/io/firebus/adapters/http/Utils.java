package io.firebus.adapters.http;

import javax.servlet.http.HttpServletRequest;

public class Utils {

	
	public static boolean acceptsFirst(HttpServletRequest req, String mime) {
		String acceptString = req.getHeader("accept");
		String[] parts = acceptString != null ? acceptString.split(",") : null;
		return parts != null && parts.length > 0 && parts[0].equalsIgnoreCase(mime) ? true : false;
	}
}
