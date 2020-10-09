package io.firebus.adapters.http;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public abstract class OutboundHandler extends Handler implements ServiceProvider {
	
	//private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	
	protected String service;
	protected String baseUrl;
	protected int timeout;
	protected HttpClient httpClient;
	
	public OutboundHandler(DataMap c, Firebus f) 
	{
		super(c, f);
		service = handlerConfig.getString("service");
		baseUrl = handlerConfig.getString("baseurl");
		timeout = handlerConfig.containsKey("timeout") ? handlerConfig.getNumber("timeout").intValue() : 10000;
		httpClient = HttpClients.createDefault();
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		Payload fbResponse = null;
		try 
		{
			logger.finest("Processing outbound request " + payload.toString());
			HttpUriRequest httpRequest = processRequest(payload);
			if(httpRequest != null)
			{
				HttpResponse response = httpClient.execute(httpRequest);
        		int respStatus = response.getStatusLine().getStatusCode(); 
        		if(respStatus >= 200 && respStatus < 400)
        		{
            		HttpEntity entity = response.getEntity();
            		if (entity != null) 
            		{
            			fbResponse = processResponse(entity);
						logger.finest(fbResponse.toString());
            		}
        		}
        		else
        		{
        			logger.severe("Http error " + respStatus + " on request " + httpRequest.toString() + " " + httpRequest.getAllHeaders() + "");
        			logger.severe("Input was " + payload.toString());
        			throw new FunctionErrorException("Http error " + respStatus + " on request " + httpRequest.toString() + " " + httpRequest.getAllHeaders() + " ");
        		}
			}
		}
		catch(Exception e) 
		{
			throw new FunctionErrorException("Error executing http request", e);
		}
		return fbResponse;
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}
	
	protected abstract HttpUriRequest processRequest(Payload payload) throws ServletException, IOException, DataException;
	
	protected abstract Payload processResponse(HttpEntity response) throws ServletException, IOException, DataException;

}
