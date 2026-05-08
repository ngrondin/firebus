package io.firebus.adapters.http;

import javax.servlet.http.HttpServletRequest;

public class Utils {

	
	public static boolean acceptsFirst(HttpServletRequest req, String mime) {
		String acceptString = req.getHeader("accept");
		String[] parts = acceptString != null ? acceptString.split(",") : null;
		return parts != null && parts.length > 0 && parts[0].equalsIgnoreCase(mime) ? true : false;
	}
	
	public static boolean isSecure(HttpServletRequest req) {
		String scheme = req.getScheme();
		String xfp = req.getHeader("X-Forwarded-Proto");
		String cffp = req.getHeader("cloudfront-forwarded-proto");
		int port = req.getServerPort();
		boolean ishttps = (scheme != null && scheme.equals("https")) || (xfp != null && xfp.equals("https")) || (cffp != null && cffp.equals("https")) || port == 443;
		return ishttps;
	}
	
	public static String getHostUrl(HttpServletRequest req) {
		boolean ishttps = Utils.isSecure(req);
		int port = req.getServerPort();
		String url = (ishttps ? "https" : "http")  + "://" + req.getServerName();
		if(port != 80 && port != 443)
			url = url + ":" + req.getServerPort();
		return url;
	}
}
