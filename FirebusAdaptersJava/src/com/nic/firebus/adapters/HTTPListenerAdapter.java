package com.nic.firebus.adapters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HTTPListenerAdapter extends Adapter
{
	protected class Handler implements HttpHandler
	{
		protected String serviceName;
		
		public Handler(String sn)
		{
			serviceName = sn;
		}

		public void handle(HttpExchange exch) throws IOException
		{
			Payload firebusRequest = new Payload();
			InputStream is = exch.getRequestBody();
			OutputStream os = exch.getResponseBody();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String query = exch.getRequestURI().getQuery();
			String path = exch.getRequestURI().getPath();
			String mime = exch.getRequestHeaders().getFirst("Content-Type");
			String accept = exch.getRequestHeaders().getFirst("Accept");
			int c = 0;
			while((c = is.read()) != -1)
				baos.write(c);
			try
			{
				String get = "";
				if(path.length() > serviceName.length() + 2)
					get = path.substring(2 + serviceName.length());

				List<String> cookies = exch.getRequestHeaders().get("Cookie");
				if(cookies != null)
				{
					for(int i = 0; i < cookies.size(); i++)
					{
						String cookie = cookies.get(i);
						String[] parts = cookie.split("=");
						firebusRequest.metadata.put(parts[0], parts[1]);
					}
				}
				
				firebusRequest.metadata.put("get", get);
				if(mime != null)
					firebusRequest.metadata.put("mime", mime);
				
				if(mime == null || (mime != null &&  mime.equals("application/x-www-form-urlencoded")))
				{
					String str = "";
					JSONObject body = new JSONObject();
					if(query != null)
					{
						str = query;
					}
					if(mime != null &&  mime.equals("application/x-www-form-urlencoded"))
					{
						if(str.length() > 0)
							str += "&";
						str += new String(baos.toByteArray());
					}
					String[] parts = str.split("&");
					for(int i = 0; i < parts.length; i++)
					{
						String[] subParts = parts[i].split("=");
						if(subParts.length == 2)
							body.put(subParts[0],  subParts[1]);
					}
					firebusRequest.setData(body.toString().getBytes());
				}
				else
				{
					if(query != null)
					{
						String[] parts = query.split("&");
						for(int i = 0; i < parts.length; i++)
						{
							String[] subParts = parts[i].split("=");
							if(subParts.length == 2)
								firebusRequest.metadata.put(subParts[0],  subParts[1]);
						}
					}
					firebusRequest.setData(baos.toByteArray());
				}				

				Payload firebusResponse = node.requestService(serviceName, firebusRequest, 10000);
				
				if(firebusResponse != null)
				{
					if(firebusResponse.metadata.get("mime") != null)
						exch.getResponseHeaders().add("Content-Type", firebusResponse.metadata.get("mime")); 
					if(firebusResponse.metadata.get("sessionid") != null)
						exch.getResponseHeaders().add("Set-Cookie", "sessionid=" + firebusResponse.metadata.get("sessionid") + "; Path=/"); 
					exch.sendResponseHeaders(200, firebusResponse.getBytes().length);
					os.write(firebusResponse.getBytes());
				}
				else
				{
					String str = "Service Unavailable";
					exch.sendResponseHeaders(200, str.length());
					os.write(str.getBytes());
				}
			}
			catch(Exception e)
			{
				String resp = "";
				if(accept.contains("application/json"))
					resp = "{\r\n\t\"error\" : \"" + e.getMessage().replaceAll("\"", "'").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n") + "\"\r\n}";
				if(accept.contains("text/html"))
					resp = "<div>" + e.getMessage() + "</div>";
				exch.sendResponseHeaders(500, resp.length());
				os.write(resp.getBytes());
				logger.severe("General error requesting service from HTTP request " + e.getMessage());
			}
			os.close();
		}
		
	}
	
	private Logger logger = Logger.getLogger("com.nic.firebus.adapters");
	
	public HTTPListenerAdapter(Firebus n, JSONObject c)
	{
		super(n, c);
		try
		{
			int port = Integer.parseInt(config.getString("port"));
			HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
			JSONList serviceList = config.getList("services");
			if(serviceList != null)
			{
				for(int i = 0; i < serviceList.size(); i++)
				{
					final String serviceName = serviceList.getString(i);
					server.createContext("/" + serviceName, new Handler(serviceName));
				}
			}
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
		} 
		catch (IOException e)
		{
			logger.severe("General error configuring HTTP Listener Adapter " + e.getMessage());
		}
	}
	

}
