package com.nic.firebus.adapters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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
			int c = 0;
			while((c = is.read()) != -1)
				baos.write(c);
			try
			{
				String query = exch.getRequestURI().getQuery();
				String path = exch.getRequestURI().getPath();
				String mime = exch.getRequestHeaders().getFirst("Content-Type");
				String resource = "";
				if(path.length() > serviceName.length() + 2)
					resource = path.substring(2 + serviceName.length());

				firebusRequest.metadata.put("resource", resource);
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
					String[] parts = query.split("&");
					for(int i = 0; i < parts.length; i++)
					{
						String[] subParts = parts[i].split("=");
						if(subParts.length == 2)
							firebusRequest.metadata.put(subParts[0],  subParts[1]);
					}
					firebusRequest.setData(baos.toByteArray());
				}				

				Payload firebusResponse = node.requestService(serviceName, firebusRequest);
				
				if(firebusResponse != null)
				{
					if(firebusResponse.metadata.get("mime") != null)
						exch.getResponseHeaders().add("Content-Type", firebusResponse.metadata.get("mime")); 
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
			server.start();
		} 
		catch (IOException e)
		{
			logger.severe("General error configuring HTTP Listener Adapter " + e.getMessage());
		}
	}
	

}
