package io.firebus.adapters.http.outbound;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.OutboundHandler;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public class OutboundGetHandler extends OutboundHandler 
{
	public OutboundGetHandler(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	protected HttpUriRequest processRequest(Payload payload) throws ServletException, IOException, DataException 
	{
		String path = this.baseUrl;
		if(path.endsWith("/"))
			path = path.substring(0, path.length() - 1);
		String relativePath = payload.metadata.get("path");
		if(relativePath != null) 
		{
			if(relativePath.startsWith("/"))
				relativePath = relativePath.substring(1);
			path = path + "/" + relativePath;
		}
		if(payload.getString().length() > 0) 
		{
			DataMap queryMap = new DataMap(payload.getString());
			String queryStr = "";
			Iterator<String> it = queryMap.keySet().iterator();
			while(it.hasNext()) 
			{
				String key = it.next();
				if(queryStr.length() > 0)
					queryStr = queryStr + "&";
				queryStr = queryStr + key + "=" + queryMap.getString(key);
			}
			
			if(queryStr.length() > 0)
			{
				if(path.contains("?"))
					path = path + "&" + queryStr;
				else
					path = path + "?" + queryStr;
			}
		}
			
		HttpGet httpget = new HttpGet(path);
		return httpget;
	}


	protected Payload processResponse(HttpEntity response) throws ServletException, IOException, DataException 
	{
		String responseStr = EntityUtils.toString(response);
		EntityUtils.consume(response);
		Payload payload = new Payload(responseStr);
		return payload;
	}

}
