package com.nic.firebus.distributables;

import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.Firebus;
import com.nic.firebus.NodeCore;
import com.nic.firebus.Payload;
import com.nic.firebus.ServiceRequest;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONObject;

public class ScriptedService extends DistributableService
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected String script;
	protected ScriptEngine js;
	protected Bindings bindings;
	protected ServiceInformation serviceInformation;

	public ScriptedService(NodeCore nc, JSONObject c)
	{
		super(nc, c);
		script = c.getString("source");
		if(script == null)
		{
			String sourceLocation = c.getString("sourcelocation");
			if(sourceLocation != null)
			{
				try
				{
					String[] parts = sourceLocation.split(":");
					ServiceRequest sr = new ServiceRequest(nodeCore, parts[0], new Payload(parts[1]), 2000);
					Payload response = sr.execute();
					if(response != null)
						script = response.getString();
					else
						logger.severe("No source file found for distributable scripted service in location " + sourceLocation);
				}
				catch(Exception e)
				{
					logger.severe("General error when retrieving the scripted service source file : " + e.getMessage());
				}
			}
		}
		js = new ScriptEngineManager().getEngineByName("javascript");
	    bindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
	    bindings.put("firebus", new Firebus(nodeCore));
	    serviceInformation = new ServiceInformation(c.getString("requestmime"), c.getString("requestcontract"), c.getString("responsemime"), c.getString("responsecontract"));
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		Payload response = new Payload();
		bindings.put("request", payload);
		bindings.put("response", response);
		try
		{
			js.eval(script);
		} 
		catch (ScriptException e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		return serviceInformation;
	}

}
