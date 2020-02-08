package io.firebus.adapters.http;

import java.io.File;
import java.util.logging.Logger;

import javax.servlet.MultipartConfigElement;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.auth.OAuth2CodeValidator;
import io.firebus.adapters.http.auth.UserPassValidator;
import io.firebus.adapters.http.inbound.GetHandler;
import io.firebus.adapters.http.inbound.PostFormHandler;
import io.firebus.adapters.http.inbound.PostJsonHandler;
import io.firebus.adapters.http.inbound.PostMultiPartHandler;
import io.firebus.adapters.http.outbound.OutboundGetHandler;
import io.firebus.adapters.http.outbound.PostHandler;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class HttpGateway implements ServiceProvider 
{
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
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
	        
	        
	        MasterHandler masterHandler = new MasterHandler();
	        Wrapper wrapper = tomcat.addServlet("/", "master", masterHandler);
	        MultipartConfigElement mpc = new MultipartConfigElement("/", 5000000, 5000000, 0);
	        wrapper.setMultipartConfigElement(mpc);
	        context.addServletMapping("/", "master");
	        
	        context.setAllowCasualMultipartParsing(true);
	
	        DataList list = config.getList("inbound");
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap inboundConfig = list.getObject(i);
		            //String service = inboundConfig.getString("service");
		            String method = inboundConfig.getString("method");
		            //String name = service + "-" + method;
		            String urlPattern = inboundConfig.getString("path");
		            InboundHandler handler = getInboundHandler(inboundConfig);
		            if(handler != null)
		            {
		            	masterHandler.addHttpHandler(urlPattern, method, handler);
		            	//Tomcat.addServlet(context, name, handler);      
		            	//context.addServletMapping(urlPattern, name);
		            }
		        }
	        }

	        list = config.getList("outbound");
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap outboundConfig = list.getObject(i);
		        	String name = outboundConfig.getString("service");
		            OutboundHandler handler = getOutboundHandler(outboundConfig);
		            if(handler != null)
		            {
		        		firebus.registerServiceProvider(name, handler, 10);
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
		            //String name = urlPattern;
		            AuthValidationHandler handler = getAuthValidationHandler(authConfig);
		            if(handler != null)
		            {
		            	masterHandler.addHttpHandler(urlPattern, "get", handler);
		            	//Tomcat.addServlet(context, name, handler);      
		            	//context.addServletMapping(urlPattern, name);
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
			else if(contentType.equals("application/x-www-form-urlencoded"))
			{
				return new PostFormHandler(inboundConfig, firebus);
			}
			else if(contentType.equals("multipart/form-data"))
			{
				return new PostMultiPartHandler(inboundConfig, firebus);
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
	
	
	private OutboundHandler getOutboundHandler(DataMap outboundConfig)
	{
		String method = outboundConfig.containsKey("method") ? outboundConfig.getString("method").toLowerCase() : "get";
		if(method.equals("get"))
		{
			return new OutboundGetHandler(outboundConfig, firebus);
		}
		else if(method.equals("post"))
		{
			return new PostHandler(outboundConfig, firebus);
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
			return new OAuth2CodeValidator(authConfig, firebus);
		}
		else if(type != null && type.equals("userpassform"))
		{
			return new UserPassValidator(authConfig, firebus);
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