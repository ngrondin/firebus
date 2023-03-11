package io.firebus.adapters.http;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Logger;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public abstract class OutboundHandler extends Handler implements ServiceProvider {
	
	protected String service;
	protected String baseUrl;
	protected int timeout;
	
	public OutboundHandler(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
		service = handlerConfig.getString("service");
		baseUrl = handlerConfig.getString("baseurl");
		timeout = handlerConfig.containsKey("timeout") ? handlerConfig.getNumber("timeout").intValue() : 10000;
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		Payload fbResponse = null;
		try 
		{
			Logger.finest("fb.http.outbound.req", payload.getDataObject());
			HttpUriRequest httpRequest = processRequest(payload);
			if(httpRequest != null)
			{
				CloseableHttpResponse response = httpGateway.getHttpClient().execute(httpRequest);
				try {
	        		int respStatus = response.getStatusLine().getStatusCode(); 
	        		HttpEntity entity = response.getEntity();
	        		if(respStatus >= 200 && respStatus < 400)
	        		{
	        			fbResponse = processResponse(entity);
	        			Logger.finest("fb.http.outbound", fbResponse);
	        		}
	        		else
	        		{
	        			String responseStr = EntityUtils.toString(entity).replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "");
	        			String errorMsg = "Http error " + respStatus + " on request " + httpRequest.toString() + " with response " + responseStr + " ";
	        			Logger.severe("fb.http.outbound.error", new DataMap("code", respStatus, "req", httpRequest.toString(), "resp", responseStr, "payload", payload.getDataObject()));
	        			throw new FunctionErrorException(errorMsg);
	        		}
				} finally {
					response.close();
				}
			}
		}
		catch(IOException | DataException | ServletException e) 
		{
			throw new FunctionErrorException("Error in outbound handler", e);
		}
		return fbResponse;
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}
	
	protected abstract HttpUriRequest processRequest(Payload payload) throws ServletException, IOException, DataException;
	
	protected abstract Payload processResponse(HttpEntity response) throws ServletException, IOException, DataException;

}
