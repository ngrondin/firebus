package io.firebus.adapters.http;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.sun.net.httpserver.HttpServer;

import io.firebus.Firebus;
import io.firebus.adapters.http.auth.AppleValidator;
import io.firebus.adapters.http.auth.NoValidator;
import io.firebus.adapters.http.auth.OAuth2CodeValidator;
import io.firebus.adapters.http.auth.UserPassValidator;
import io.firebus.adapters.http.handlers.AuthValidationHandler;
import io.firebus.adapters.http.handlers.InboundHandler;
import io.firebus.adapters.http.handlers.LogoutHandler;
import io.firebus.adapters.http.handlers.MasterHandler;
import io.firebus.adapters.http.handlers.OutboundHandler;
import io.firebus.adapters.http.handlers.SecurityHandler;
import io.firebus.adapters.http.handlers.inbound.FileStreamHandler;
import io.firebus.adapters.http.handlers.inbound.GetHandler;
import io.firebus.adapters.http.handlers.inbound.PostFormHandler;
import io.firebus.adapters.http.handlers.inbound.PostJsonHandler;
import io.firebus.adapters.http.handlers.inbound.PostMultiPartHandler;
import io.firebus.adapters.http.handlers.outbound.GeneralOutboundHandler;
import io.firebus.adapters.http.handlers.outbound.OutboundGetHandler;
import io.firebus.adapters.http.handlers.outbound.PostHandler;
import io.firebus.adapters.http.handlers.security.JWTCookie;
import io.firebus.adapters.http.handlers.websocket.StreamGatewayWSHandler;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;

@SuppressWarnings("restriction")
public class HttpGateway
{
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	protected int port;
	protected String contextPath;
	protected Firebus firebus;
	protected MasterHandler masterHandler = new MasterHandler();
    protected List<SecurityHandler> securityHandlerList = new ArrayList<SecurityHandler>(); 
	protected CloseableHttpClient httpclient;
	
	public HttpGateway(DataMap c, Firebus f) {
		firebus = f;
		processConfig(c);
		createHttpClient();
		createLogoutHandler();
		startHttpServer();
	}
	
	public HttpGateway(int p, String cp, Firebus f) {
		port = p;
		contextPath = cp;
		firebus = f;
		createHttpClient();
		createLogoutHandler();
		startHttpServer();
	}
	
	protected void createHttpClient() {
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
	}
	
	protected void createLogoutHandler() {
        LogoutHandler logoutHandler = new LogoutHandler(firebus, new DataMap());
        logoutHandler.setSecuritytHandlers(securityHandlerList);
        masterHandler.addHttpHandler("/logout", null, null, logoutHandler);
	}
	
	public void addSecurityHandler(SecurityHandler handler) {
    	securityHandlerList.add(handler);
	}
	
	public void addHttpHandler(String urlPattern, String method, String contentType, InboundHandler handler) {
		masterHandler.addHttpHandler(urlPattern, method, contentType, handler);
	}
	
	public void addOutboundHandler(String name, OutboundHandler handler) {
		firebus.registerServiceProvider(name, handler, 10);
	}
	
	protected void processConfig(DataMap config) {
        try 
        {
        	String portStr = config.getString("port");
	        port = portStr != null && !portStr.equals("") ? Integer.parseInt(portStr) : 80; 
	        contextPath = config.containsKey("path") ? config.getString("path") : "/";
	        if(contextPath.endsWith("/") && contextPath.length() > 1) contextPath = contextPath.substring(0, contextPath.length() - 1);
	        if(!contextPath.startsWith("/")) contextPath = "/" + contextPath;
	        if(config.containsKey("rootforward"))
	        	masterHandler.setRootForward(config.getString("rootforward"));
	        
        	Map<String, SecurityHandler> securityHandlerMap = new HashMap<String, SecurityHandler>();
		    DataList list = config.getList("security");
		    if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap securityConfig = list.getObject(i);
		        	String name = securityConfig.getString("name");
		    		String type = securityConfig.containsKey("type") ? securityConfig.getString("type").toLowerCase() : "jwtcookie";
		    		SecurityHandler handler = null;
		    		if(type.equals("jwtcookie")) {
		    			handler = new JWTCookie(securityConfig, getHttpClient());
		    		} 
		            if(handler != null) {
		            	addSecurityHandler(handler);
		            	securityHandlerMap.put(name, handler);
		            }
		        }
	        }
	        

	        list = config.getList("authvalidation");
	        String publicHost = config.getString("publichost");
	        if(list != null)
	        {
		        for(int i = 0; i < list.size(); i++)
		        {
		        	DataMap authConfig = list.getObject(i);
		            String method = authConfig.getString("method");
		            String contentType = authConfig.getString("contenttype");
		            String urlPattern = authConfig.getString("path");
		            String security = authConfig.getString("security");
		    		String type = authConfig.getString("type").toLowerCase();
		    		AuthValidationHandler handler = null;
		    		if(type != null && type.equals("oauth2code")) {
		    			handler =  new OAuth2CodeValidator(firebus, authConfig, getHttpClient());
		    		} else if(type != null && type.equals("apple")) {
		    			handler =  new AppleValidator(firebus, authConfig, getHttpClient());
		    		} else if(type != null && type.equals("userpassform")) {
		    			handler =  new UserPassValidator(firebus, authConfig, getHttpClient());
		    		} else if(type != null && type.equals("novalidation")) {
		    			handler =  new NoValidator(firebus, authConfig, getHttpClient());
		    		}
		            if(handler != null) {
		            	if(security != null) {
		            		SecurityHandler securityHandler = securityHandlerMap.get(security); 
		            		handler.setSecurityHandler(securityHandler);
		            		securityHandler.addAuthValidationHandler(handler);		            		
		            	}
		            	if(publicHost != null)
		            		handler.setPublicHost(publicHost);
		            	addHttpHandler(urlPattern, method, contentType, handler);
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
		    		String type = inboundConfig.getString("type");
		    		InboundHandler handler = null;
		    		if(type != null && type.equals("filestream")) {
		    			handler =  new FileStreamHandler(firebus, inboundConfig);
		    		} else if(method == null || method.equals("get")) {
		    			handler =  new GetHandler(firebus, inboundConfig);
		    		} else if(method != null && method.equals("post")) {
		    			if(contentType == null || contentType.equals("application/json")) {
		    				handler =  new PostJsonHandler(firebus, inboundConfig);
		    			} else if(contentType.equals("application/x-www-form-urlencoded")) {
		    				handler =  new PostFormHandler(firebus, inboundConfig);
		    			} else if(contentType.equals("multipart/form-data")) {
		    				handler =  new PostMultiPartHandler(firebus, inboundConfig);
		    			} else {
		    				handler =  new PostJsonHandler(firebus, inboundConfig);
		    			}
		    		} else {
		    			handler = new GetHandler(firebus, inboundConfig);
		    		}
		            if(handler != null) {
		            	if(security != null)
		            		handler.setSecurityHandler(securityHandlerMap.get(security));
		            	addHttpHandler(urlPattern, method, contentType, handler);
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
		    		String type = wsConfig.getString("type").toLowerCase();
		    		WebsocketHandler handler = null;
		    		if(type.equals("stream")) {
		    			Class<?> clz = StreamGatewayWSHandler.class;
		    			handler =  new WebsocketHandler(firebus, wsConfig, clz);
		    		}
		            if(handler != null) {
		            	if(security != null)
		            		handler.setSecurityHandler(securityHandlerMap.get(security));
		            	addHttpHandler(urlPattern, "get", null, handler);
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
		    		String method = outboundConfig.getString("method");
		    		OutboundHandler handler = null;
		    		if(method != null && method.toLowerCase().equals("get")) {
		    			handler =  new OutboundGetHandler(firebus, outboundConfig, getHttpClient());
		    		} else if(method != null && method.toLowerCase().equals("post")) {
		    			handler =  new PostHandler(firebus, outboundConfig, getHttpClient());
		    		} else {
		    			handler =  new GeneralOutboundHandler(firebus, outboundConfig, getHttpClient());
		    		}
		            if(handler != null)
		        		addOutboundHandler(name, handler);
		        }
	        }
		} 
        catch (Exception e) 
        {
        	logger.severe("Error configuring the Http Gateway : " + e.getMessage());
        	e.printStackTrace();
		}
	}
	
	public void startHttpServer() {
		try
		{
	        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
	        server.createContext(contextPath, masterHandler);
	        server.setExecutor(Executors.newCachedThreadPool()); 
	        server.start();
		}
        catch (Exception e) 
        {
        	logger.severe("Error starting the Http Gateway : " + e.getMessage());
        	e.printStackTrace();
		}
	}
	
	public CloseableHttpClient getHttpClient() {
		return httpclient;
	}




}
