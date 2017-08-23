package com.nic.firebus;

import java.util.logging.Logger;

import com.nic.firebus.interfaces.BusFunction;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.FunctionListener;
import com.nic.firebus.interfaces.ServiceProvider;

public class FunctionWorker extends Thread
{
	private Logger logger = Logger.getLogger(FunctionWorker.class.getName());
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
		if(inboundMessage.getType() == Message.MSGTYPE_REQUESTSERVICE  &&  busFunction instanceof ServiceProvider)
		{
			logger.info("Executing Service Provider");
			byte[] returnPayload = ((ServiceProvider)busFunction).requestService(payload);
			if(functionListener != null)
				functionListener.functionCallback(inboundMessage, returnPayload);
		}
		else if(inboundMessage.getType() == Message.MSGTYPE_PUBLISH  &&  busFunction instanceof Consumer)
		{
			logger.info("Executing Consumer");
			((Consumer)busFunction).consume(payload);
		}
		
	}

}
