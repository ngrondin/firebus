package com.nic.firebus;

import com.nic.firebus.interfaces.BusFunction;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.FunctionListener;
import com.nic.firebus.interfaces.ServiceProvider;

public class FunctionWorker extends Thread
{
	protected BusFunction busFunction;
	protected Message inboundMessage;
	protected FunctionListener functionListener;
	
	public FunctionWorker(BusFunction f, Message im, FunctionListener fl)
	{
		busFunction = f;
		inboundMessage = im;
		functionListener = fl;
		start();
	}
	
	public void run()
	{
		byte[] payload = inboundMessage.getPayload();
		if(busFunction instanceof ServiceProvider)
		{
			byte[] returnPayload = ((ServiceProvider)busFunction).requestService(payload);
			if(functionListener != null)
				functionListener.functionCallback(inboundMessage, returnPayload);
		}
		else if(busFunction instanceof Consumer)
		{
			((Consumer)busFunction).consume(payload);
		}
		
	}

}
