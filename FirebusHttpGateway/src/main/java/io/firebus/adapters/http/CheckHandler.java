package io.firebus.adapters.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public class CheckHandler extends HttpHandler {
	protected List<SecurityHandler> securityHandlers;
		
	public CheckHandler(HttpGateway gw, Firebus f, DataMap c) {
		super(gw, f, c);
	}
	
	public void setSecuritytHandlers(List<SecurityHandler> sh) {
		securityHandlers = sh;
	}

	protected void httpService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		DataList list = new DataList();
		if(securityHandlers != null) {
			for(SecurityHandler sh: securityHandlers) {
				DataMap data = sh.getCheckData(req);
				if(data != null) list.add(data);
			}
		}
        PrintWriter writer = resp.getWriter();
		if(Utils.acceptsFirst(req, "text/html")) {
			if(list.size() == 0) {
				writer.println("<html><body>Unauthenticated</body></html>");
			} else {
				writer.println("<html><body>Authenticated</body></html>");
			}				
		} else if(Utils.acceptsFirst(req, "application/json")) {
			if(list.size() == 0) {
				writer.println((new DataMap("error", "invalid")).toString(true));
			} else if(list.size() == 1) {
				writer.println(list.getObject(0).toString(true));
			} else {
				writer.println(list.toString(true));
			}
		}
	}


}
