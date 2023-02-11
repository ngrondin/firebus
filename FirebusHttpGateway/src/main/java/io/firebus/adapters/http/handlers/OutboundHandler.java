package io.firebus.adapters.http.handlers;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;

public abstract class OutboundHandler extends Handler implements ServiceProvider {
	
	//private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	
	protected String service;
	protected String baseUrl;
	protected CloseableHttpClient httpClient;
	protected int timeout;
	
	public OutboundHandler(Firebus f, DataMap c, CloseableHttpClient hc) 
	{
		super(f, c);
		httpClient = hc;
		service = handlerConfig.getString("service");
		baseUrl = handlerConfig.getString("baseurl");
		timeout = handlerConfig.containsKey("timeout") ? handlerConfig.getNumber("timeout").intValue() : 10000;
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		Payload fbResponse = null;
		try 
		{
			logger.finest("Processing outbound request " + payload.toString());
			HttpUriRequest httpRequest = processRequest(payload);
			if(httpRequest != null)
			{
				CloseableHttpResponse response = httpClient.execute(httpRequest);
				try {
	        		int respStatus = response.getStatusLine().getStatusCode(); 
	        		HttpEntity entity = response.getEntity();
	        		if(respStatus >= 200 && respStatus < 400)
	        		{
	        			fbResponse = processResponse(entity);
						logger.finest(fbResponse.toString());
	        		}
	        		else
	        		{
	        			String responseStr = EntityUtils.toString(entity).replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "");
	        			String errorMsg = "Http error " + respStatus + " on request " + httpRequest.toString() + " with response " + responseStr + " ";
	        			logger.severe(errorMsg);
	        			logger.severe("Input was " + payload.toString());
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
