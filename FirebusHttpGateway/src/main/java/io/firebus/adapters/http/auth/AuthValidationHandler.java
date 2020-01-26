package io.firebus.adapters.http.auth;


import io.firebus.Firebus;
import io.firebus.adapters.http.Handler;
import io.firebus.utils.DataMap;

public abstract class AuthValidationHandler extends Handler
{
	private static final long serialVersionUID = 1L;

	public AuthValidationHandler(DataMap c, Firebus fb) 
	{
		super(c, fb);
	}

}
