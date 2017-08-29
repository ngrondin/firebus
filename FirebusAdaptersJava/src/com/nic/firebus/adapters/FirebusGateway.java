package com.nic.firebus.adapters;

import com.nic.firebus.utils.JSONObject;

public class FirebusGateway extends StandaloneAdapter
{

	public FirebusGateway(JSONObject config)
	{
		super(config);
	}

	public static void main(String[] args)
	{
		initiateStandalone(args);
	}
}
