package com.nic.firebus.adapters;

import com.nic.firebus.Node;
import com.nic.firebus.utils.JSONObject;

public class ScriptedFunction extends FirebusAdapter
{
	protected String script;
	
	public ScriptedFunction(Node n, JSONObject c) 
	{
		super(n, c);
		script = c.getString("source");
	}
	
	

}
