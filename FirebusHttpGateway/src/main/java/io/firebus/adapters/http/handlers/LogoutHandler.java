package io.firebus.adapters.http.handlers;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.data.DataMap;

public class LogoutHandler extends InboundHandler {
	protected List<SecurityHandler> securityHandlers;
		
	public LogoutHandler(Firebus f, DataMap c) {
		super(f, c);
	}
	
	public void setSecuritytHandlers(List<SecurityHandler> sh) {
		securityHandlers = sh;
	}

	protected HttpResponse httpService(HttpRequest req) {
		HttpResponse resp = new HttpResponse();
		if(securityHandlers != null) {
			for(SecurityHandler securityHandler: securityHandlers) {
				securityHandler.enrichLogoutResponse(resp);
			}
		}
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Login</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>");
        sb.append("body{}");
        sb.append(".main{position:fixed; display:flex; flex-direction:column; align-items:center; top:50%; left: 50%; transform: translate(-50%, -50%); font-family:sans-serif; font-size:larger; border:1px solid lightgrey; padding:15px; border-radius:5px;}");
        sb.append(".logo{padding:5px;width:40px;}");
        sb.append(".title{color:black; padding:10px; white-space:nowrap;}");
        sb.append(".return{padding: 5px;}");
        sb.append(".option{display:flex; flex-direction:row; align-items:center;padding:5px;}");
        sb.append("a{display:flex; flex-direction:row; align-items:center;}");
        sb.append("a:link {color:grey; text-decoration:none;} a:visited  {color:grey; text-decoration:none;} a:hover {color:black; text-decoration:none;} a:active {color:grey; text-decoration:none;}");
        sb.append("img {padding-right:10px;}");
        sb.append("</style></head>");
        sb.append("<body><div class=\"main\">");
        sb.append("<img class=\"logo\" src=\"data:image/png;base64,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\">");
        sb.append("<div class=\"title\">You have logged out</div>");
        sb.append("<div class=\"return\"><a id=\"return\" href=\"/\">Return</a></div>");
        sb.append("<script>r = (new URL(document.location)).searchParams.get('return'); a = (new URL(document.location)).searchParams.get('auto'); if(a == 'return' && r != null) {setTimeout(function() {window.location = r}, 2000);} else if(a == 'close') {setTimeout(function() {window.close()}, 2000);} else {document.getElementById('return').href = (r != null ? r : '/');}</script>");
        sb.append("</div></body></html>");
		resp.setBody(sb.toString());
		return resp;
	}
}