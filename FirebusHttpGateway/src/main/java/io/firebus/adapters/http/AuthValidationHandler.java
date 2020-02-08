package io.firebus.adapters.http;


import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class AuthValidationHandler extends HttpHandler
{
	public AuthValidationHandler(DataMap c, Firebus fb) 
	{
		super(c, fb);
	}

}
