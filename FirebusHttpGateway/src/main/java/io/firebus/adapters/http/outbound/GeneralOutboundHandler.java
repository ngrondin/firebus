package io.firebus.adapters.http.outbound;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.OutboundHandler;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public class GeneralOutboundHandler extends OutboundHandler {

	public GeneralOutboundHandler(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	@Override
	protected HttpUriRequest processRequest(Payload payload) throws ServletException, IOException, DataException {
		DataMap request = new DataMap(payload.getString());
		String url = (this.baseUrl != null && request.containsKey("path") ? baseUrl + "/" + request.getString("path") : request.getString("url"));
		String method = request.getString("method");
		
		if(method.equals("post")) 
		{
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new ByteArrayEntity(request.get("body").toString().getBytes()));
			return httppost;
		}
		else if(method.equals("get"))
		{
			HttpGet httpget = new HttpGet(url);
			return httpget;			
		}
		else
		{
			return null;
		}
	}

	@Override
	protected Payload processResponse(HttpEntity response) throws ServletException, IOException, DataException {
		String responseStr = EntityUtils.toString(response);
		EntityUtils.consume(response);
		Payload payload = new Payload(responseStr);
		return payload;
	}

}
