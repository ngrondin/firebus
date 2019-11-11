package com.nic.firebus.adapters.http;

import java.io.File;
import java.util.logging.Logger;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.adapters.http.auth.AuthValidationHandler;
import com.nic.firebus.adapters.http.auth.OAuth2CodeValidator;
import com.nic.firebus.adapters.http.inbound.GetHandler;
import com.nic.firebus.adapters.http.inbound.InboundHandler;
import com.nic.firebus.adapters.http.inbound.PostJsonHandler;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class HttpGateway implements ServiceProvider 
{
	private Logger logger = Logger.getLogger("com.nic.firebus.adapters");
	protected Firebus firebus;
	protected DataMap config;
	protected Tomcat tomcat;
	
	public HttpGateway(DataMap c, Firebus f) {
		config = c;
		firebus = f;
		
        try 
        {
	        tomcat = new Tomcat();
	        tomcat.setBaseDir("temp");
	        
	        int port = config.containsKey("port") ? Integer.parseInt(config.getString("port")) : 80; 
	        tomcat.setPort(port);
	         
	        String contextPath = config.containsKey("path") ? config.getString("path") : "/";
	        String docBase = new File(".").getAbsolutePath();
	        Context context = tomcat.addContext(contextPath, docBase);
	
	        DataList list = config.getList("inbound");
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap inboundConfig = list.getObject(i);
		            String name = inboundConfig.getString("service");
		            String urlPattern = inboundConfig.getString("path");
		            InboundHandler handler = getInboundHandler(inboundConfig);
		            if(handler != null)
		            {
		            	Tomcat.addServlet(context, name, handler);      
		            	context.addServletMapping(urlPattern, name);
		            }
		        }
	        }

	        list = config.getList("authvalidation");
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap authConfig = list.getObject(i);
		            String urlPattern = authConfig.getString("path");
		            String name = urlPattern;
		            AuthValidationHandler handler = getAuthValidationHandler(authConfig);
		            if(handler != null)
		            {
		            	Tomcat.addServlet(context, name, handler);      
		            	context.addServletMapping(urlPattern, name);
		            }
		        }
	        }

			tomcat.start();
		} 
        catch (Exception e) 
        {
        	logger.severe("Error initiating the Http Gateway : " + e.getMessage());
		}
	}
	
	private InboundHandler getInboundHandler(DataMap inboundConfig)
	{
		String method = inboundConfig.containsKey("method") ? inboundConfig.getString("method").toLowerCase() : "get";
		String contentType = inboundConfig.containsKey("contenttype") ? inboundConfig.getString("contenttype").toLowerCase() : "application/json";
		if(method.equals("get"))
		{
			return new GetHandler(inboundConfig, firebus);
		}
		else if(method.equals("post"))
		{
			if(contentType.equals("application/json"))
			{
				return new PostJsonHandler(inboundConfig, firebus);
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	
	private AuthValidationHandler getAuthValidationHandler(DataMap authConfig)
	{
		String type = authConfig.getString("type").toLowerCase();
		if(type != null && type.equals("oauth2code"))
		{
			return new OAuth2CodeValidator(authConfig);
		}
		else
		{
			return null;
		}
	}
		
	public Payload service(Payload payload) throws FunctionErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceInformation getServiceInformation() {
		// TODO Auto-generated method stub
		return null;
	}

}
