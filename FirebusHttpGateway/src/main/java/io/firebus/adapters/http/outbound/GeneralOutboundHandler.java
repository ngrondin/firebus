package io.firebus.adapters.http.outbound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
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
		HttpUriRequest httpRequest = null;
		if(method.equals("post")) 
		{
			HttpPost httpPost = new HttpPost(url);
			if(request.containsKey("body")) {
				httpPost.setEntity(new ByteArrayEntity(request.get("body").toString().getBytes()));
			} else if(request.containsKey("form")) {
				List<NameValuePair> formParams = new ArrayList<NameValuePair>();
				Iterator<String> it = request.getObject("form").keySet().iterator();
				while(it.hasNext()) {
					String key = it.next();
					formParams.add(new BasicNameValuePair(key, request.getObject("form").getString(key)));
				}				
				httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
			}
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpRequest = httpPost;
		}
		else if(method.equals("get"))
		{
			httpRequest = new HttpGet(url);
		}
		
		if(request.containsKey("cookie")) {
			String cookie = "";
			Iterator<String> it = request.getObject("cookie").keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				cookie = cookie + key + "=" + request.getObject("cookie").getString(key) + (it.hasNext() ? ";" : "");
			}
			httpRequest.setHeader("Cookie", cookie);
		}
		if(request.containsKey("authorization")) {
			String authorization = request.getString("authorization");
			httpRequest.setHeader("Authorization", authorization);
		}
		if(request.containsKey("headers")) {
			Iterator<String> it = request.getObject("headers").keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				httpRequest.setHeader(key, request.getObject("headers").getString(key));
			}
		}
		return httpRequest;
	}

	@Override
	protected Payload processResponse(HttpEntity response) throws ServletException, IOException, DataException {
		String responseStr = EntityUtils.toString(response);
		EntityUtils.consume(response);
		Payload payload = new Payload(responseStr);
		return payload;
	}

}
