package com.nic.firebus.adapters.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nic.firebus.utils.DataMap;

public class ServiceHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ServiceHandler(DataMap c) {
		
	}
	
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
         
        writer.println("<html><title>Welcome</title><body>");
        writer.println("<h1>Have a Great Day!</h1>");
        writer.println("</body></html>");
    }	
}
