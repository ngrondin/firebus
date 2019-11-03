package com.nic.firebus.adapters.http.auth;


import com.nic.firebus.adapters.http.Handler;
import com.nic.firebus.utils.DataMap;

public abstract class AuthValidationHandler extends Handler
{
	private static final long serialVersionUID = 1L;

	public AuthValidationHandler(DataMap c) 
	{
		super(c);
	}

}
