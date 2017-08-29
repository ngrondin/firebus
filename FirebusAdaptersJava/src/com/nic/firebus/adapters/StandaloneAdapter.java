package com.nic.firebus.adapters;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;

import com.nic.firebus.Node;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public abstract class StandaloneAdapter
{

	public static class CurrentClassGetter extends SecurityManager 
	{
	    public Class<?> getCallerClass() 
	    {
	    	Class<?> c = getClassContext()[2];
	    	return c;
	    }
	}
	
	protected Node node;

	public StandaloneAdapter(JSONObject config)
	{
		node = new Node(config.getString("network"), config.getString("password"));
		JSONList knownAddresses = config.getList("knownaddresses");
		for(int i = 0; i < knownAddresses.size(); i++)
		{
			node.addKnownNodeAddress(knownAddresses.getMap(i).getString("address"), Integer.parseInt(knownAddresses.getMap(i).getString("port")));
		}
	}
	
	protected static void initiateStandalone(String args[])
	{
		if(args.length > 0)
		{
			try
			{
				JSONObject config = new JSONObject(new FileInputStream(args[0]));
				Class<?> c = (new CurrentClassGetter()).getCallerClass();
				Constructor<?> cons = c.getConstructor(new Class[]{JSONObject.class});
				StandaloneAdapter a = (StandaloneAdapter)cons.newInstance(new Object[]{config});
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
	}
	
}
