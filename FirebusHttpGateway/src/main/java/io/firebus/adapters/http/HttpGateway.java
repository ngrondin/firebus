package io.firebus.adapters.http;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.idm.AppleIDM;
import io.firebus.adapters.http.idm.NoValidator;
import io.firebus.adapters.http.idm.OAuth2IDM;
import io.firebus.adapters.http.inbound.FileStreamHandler;
import io.firebus.adapters.http.inbound.GetHandler;
import io.firebus.adapters.http.inbound.PostFormHandler;
import io.firebus.adapters.http.inbound.PostJsonHandler;
import io.firebus.adapters.http.inbound.PostMultiPartHandler;
import io.firebus.adapters.http.outbound.GeneralOutboundHandler;
import io.firebus.adapters.http.outbound.OutboundGetHandler;
import io.firebus.adapters.http.outbound.PostHandler;
import io.firebus.adapters.http.security.JWTCookie;
import io.firebus.adapters.http.websocket.StreamGatewayWSHandler;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Logger;

public class HttpGateway implements ServiceProvider 
{
	protected String name;
	protected Firebus firebus;
	protected DataMap config;
	protected Tomcat tomcat;
	protected CloseableHttpClient httpclient;
	
	public HttpGateway(DataMap c, Firebus f) {
		config = c;
		firebus = f;
		init();
	}
	
	public HttpGateway(String n, DataMap c, Firebus f) {
		name = n;
		config = c;
		firebus = f;
		init();
	}
	
	protected void init() {
        try 
        {
        	String portStr = config.getString("port");
	        int port = portStr != null && !portStr.equals("") ? Integer.parseInt(portStr) : 80; 
	        tomcat = new Tomcat();
	        tomcat.setBaseDir("temp");
	        tomcat.getConnector().setPort(port);
	        tomcat.getConnector().setAttribute("compression", "on");
	        tomcat.getConnector().setAttribute("compressableMimeType", "text/html,text/xml,text/plain,application/json,application/javascript");
	        
	        String contextPath = config.containsKey("path") ? config.getString("path") : "/";
	        String docBase = new File(".").getAbsolutePath();
	        Context context = tomcat.addContext(contextPath, docBase);
	        
	        MasterHandler masterHandler = new MasterHandler();
	        Wrapper wrapper = tomcat.addServlet("/", "master", masterHandler);
	        MultipartConfigElement mpc = new MultipartConfigElement(docBase, 5000000, 5000000, 0);
	        wrapper.setMultipartConfigElement(mpc);
	        context.addServletMapping("/", "master");
	        context.setAllowCasualMultipartParsing(true);
	        if(config.containsKey("rootforward"))
	        	masterHandler.setRootForward(config.getString("rootforward"));
	        //String publicHost = config.getString("publichost");
	        
	        RequestConfig requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(1000)
					.setConnectTimeout(5000)
					.setSocketTimeout(60000)
					.build();
	        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		    connectionManager.setMaxTotal(100);
		    connectionManager.setDefaultMaxPerRoute(50);
		    httpclient = HttpClients.custom()
		    		.setConnectionManager(connectionManager)
		    		.setDefaultRequestConfig(requestConfig)
		    		.build();

		    DataList list = config.getList("security");
	        Map<String, SecurityHandler> securityHandlerMap = new HashMap<String, SecurityHandler>();
	        List<SecurityHandler> securityHandlerList = new ArrayList<SecurityHandler>();
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap securityConfig = list.getObject(i);
		        	String name = securityConfig.getString("name");
		            SecurityHandler handler = getSecurityHandler(securityConfig);
		            if(handler != null) {
		            	securityHandlerMap.put(name, handler);
		            	securityHandlerList.add(handler);
		            }
		        }
	        }
	        
	        LogoutHandler logoutHandler = new LogoutHandler(this, firebus, new DataMap());
	        logoutHandler.setSecuritytHandlers(securityHandlerList);
	        masterHandler.setLogouHander(logoutHandler);
	        
	        CommandHandler commandHandler = new CommandHandler(this, firebus, new DataMap());
	        commandHandler.setSecuritytHandlers(securityHandlerList);
	        firebus.registerConsumer(name, commandHandler, 10);

	        list = config.getList("idms");
	        List<IDMHandler> idmHandlers = new ArrayList<IDMHandler>();
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap idmConfig = list.getObject(i);
		            String method = idmConfig.getString("method");
		            String contentType = idmConfig.getString("contenttype");
		            String urlPattern = idmConfig.getString("path");
		            String security = idmConfig.getString("security");
		            IDMHandler handler = getIDMHandler(idmConfig);
		            if(handler != null) {
		            	if(security != null) {
		            		SecurityHandler securityHandler = securityHandlerMap.get(security); 
		            		handler.setSecurityHandler(securityHandler);
		            		securityHandler.addIDMHandler(handler);	            		
		            	}
		            	masterHandler.addHttpHandler(urlPattern, method, contentType, handler);
		            	idmHandlers.add(handler);
		            }
		        }
	        }


	        list = config.getList("inbound");
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap inboundConfig = list.getObject(i);
		            String method = inboundConfig.getString("method");
		            String contentType = inboundConfig.getString("contenttype");
		            String urlPattern = inboundConfig.getString("path");
		            String security = inboundConfig.getString("security");
		            InboundHandler handler = getInboundHandler(inboundConfig);
		            if(handler != null) {
		            	if(security != null)
		            		handler.setSecurityHandler(securityHandlerMap.get(security));
		            	masterHandler.addHttpHandler(urlPattern, method, contentType, handler);
		            }
		        }
	        }
	        
	        list = config.getList("websockets");
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap wsConfig = list.getObject(i);
		        	String name = wsConfig.getString("name");
		            String urlPattern = wsConfig.getString("path");
		            String security = wsConfig.getString("security");
		            WebsocketHandler handler = getWebsocketHandler(wsConfig);
		            if(handler != null)
		            {
		            	if(security != null)
		            		handler.setSecurityHandler(securityHandlerMap.get(security));
		            	masterHandler.addHttpHandler(urlPattern, "get", null, handler);
		            	if(handler instanceof ServiceProvider)
		            		firebus.registerServiceProvider(name, (ServiceProvider)handler, 10);
		            	else if(handler instanceof Consumer)
		            		firebus.registerConsumer(name, (Consumer)handler, 10);
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
		        		firebus.registerServiceProvider(name, handler, 10);
		        }
	        }

			tomcat.start();
		} 
        catch (Exception e) 
        {
        	Logger.severe("fb.http.init", e);
        	e.printStackTrace();
		}
	}
	
	public CloseableHttpClient getHttpClient() {
		return httpclient;
	}

	private SecurityHandler getSecurityHandler(DataMap securityConfig)
	{
		String type = securityConfig.containsKey("type") ? securityConfig.getString("type").toLowerCase() : "jwtcookie";
		if(type.equals("jwtcookie"))
		{
			return new JWTCookie(this, securityConfig);
		}
		else
		{
			return null;
		}
	}
	
	private InboundHandler getInboundHandler(DataMap inboundConfig)
	{
		String method = inboundConfig.containsKey("method") ? inboundConfig.getString("method").toLowerCase() : "get";
		String contentType = inboundConfig.containsKey("contenttype") ? inboundConfig.getString("contenttype").toLowerCase() : "application/json";
		String type = inboundConfig.getString("type");
		if(type != null && type.equals("filestream")) {
			return new FileStreamHandler(this, firebus, inboundConfig);
		}
		else if(method.equals("get"))
		{
			return new GetHandler(this, firebus, inboundConfig);
		}
		else if(method.equals("post"))
		{
			if(contentType.equals("application/json"))
			{
				return new PostJsonHandler(this, firebus, inboundConfig);
			}
			else if(contentType.equals("application/x-www-form-urlencoded"))
			{
				return new PostFormHandler(this, firebus, inboundConfig);
			}
			else if(contentType.equals("multipart/form-data"))
			{
				return new PostMultiPartHandler(this, firebus, inboundConfig);
			}
			else
			{
				return new PostJsonHandler(this, firebus, inboundConfig);
			}
		}
		else
		{
			return new GetHandler(this, firebus, inboundConfig);
		}
	}
	
	
	private OutboundHandler getOutboundHandler(DataMap outboundConfig)
	{
		String method = outboundConfig.getString("method");
		if(method != null && method.toLowerCase().equals("get"))
		{
			return new OutboundGetHandler(this, firebus, outboundConfig);
		}
		else if(method != null && method.toLowerCase().equals("post"))
		{
			return new PostHandler(this, firebus, outboundConfig);
		}
		else
		{
			return new GeneralOutboundHandler(this, firebus, outboundConfig);
		}
	}
	
	private WebsocketHandler getWebsocketHandler(DataMap wsConfig)
	{
		String type = wsConfig.containsKey("type") ? wsConfig.getString("type").toLowerCase() : "echo";
		if(type.equals("stream"))
		{
			Class<?> clz = StreamGatewayWSHandler.class;
			return new WebsocketHandler(this, firebus, wsConfig, clz);
		}
		else 
		{
			return null;
		}
		
	}
	
	private IDMHandler getIDMHandler(DataMap authConfig)
	{
		String type = authConfig.getString("type").toLowerCase();
		if(type != null && type.equals("oauth2code"))
		{
			return new OAuth2IDM(this, firebus, authConfig);
		}
		else if(type != null && type.equals("apple"))
		{
			return new AppleIDM(this, firebus, authConfig);
		}
		else if(type != null && type.equals("novalidation"))
		{
			return new NoValidator(this, firebus, authConfig);
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
	
	public String getServiceName() {
		return name;
	}
	
	public String getPublicHost() {
		return this.config.getString("publichost");
	}



}
