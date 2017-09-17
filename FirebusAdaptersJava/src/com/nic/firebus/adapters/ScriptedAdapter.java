package com.nic.firebus.adapters;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONObject;

public class ScriptedAdapter extends Adapter implements ServiceProvider
{
	private Logger logger = Logger.getLogger("com.nic.firebus.adapters");
	protected String script;
	protected ScriptEngine js;
	protected Bindings bindings;
	
	public ScriptedAdapter(Firebus n, JSONObject c) 
	{
		super(n, c);
		script = c.getString("source");
		js = new ScriptEngineManager().getEngineByName("javascript");
	    bindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		HashMap<String, String> metadata = new HashMap<String, String>();
		Payload response = new Payload(metadata, null);
		bindings.put("request", payload);
		bindings.put("response", response);
		try
		{
			js.eval(script);
		} 
		catch (ScriptException e)
		{
			logger.severe(e.getMessage());
		}
		return response;
	}
	
	

}
